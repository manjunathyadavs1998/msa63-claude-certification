package course.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import course.ai.model.ClaudeModel;
import course.ai.model.ClaudeProperties;
import course.ai.model.ModelException;
import course.ai.model.ModelPort;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** The real SDK talking real HTTP to a local fake. Nothing here reaches Anthropic. */
class ClaudeModelTest {

    private static final ObjectMapper JSON = new ObjectMapper();
    private FakeAnthropicServer server;
    private AnthropicClient client;
    private ClaudeModel model;

    @BeforeEach
    void start() throws Exception {
        server = new FakeAnthropicServer();
        ClaudeProperties properties = new ClaudeProperties("claude-opus-5-5", 16000,
                Duration.ofSeconds(5), 2, "medium", server.url(), "test-key");
        client = AnthropicOkHttpClient.builder()
                .apiKey("test-key")
                .baseUrl(server.url())
                .timeout(properties.timeout())
                .maxRetries(properties.maxRetries())
                .build();
        model = new ClaudeModel(client, properties);
    }

    @AfterEach
    void stop() {
        client.close();
        server.close();
    }

    private static ModelPort.Request ask(String text) {
        return new ModelPort.Request("You are a support agent.",
                List.of(new ModelPort.UserText(text)), List.of(new ModelPort.ToolDef(
                        "lookup_order", "Looks up one order.",
                        Map.of("order_id", Map.of("type", "string")),
                                List.of("order_id"))));
    }

    @Test
    void sendsWhatWeThinkItSends() throws Exception {
        server.reply(200, FakeAnthropicServer.message("end_turn",
                "[{\"type\":\"text\",\"text\":\"Hello\"}]"));
        ModelPort.Turn turn = model.send(ask("Hi"));
        JsonNode body = JSON.readTree(server.seen.get(0).body());
        System.out.println("LAB|request path: " + server.seen.get(0).path());
        System.out.println("LAB|request body: " + body);
        System.out.println("LAB|turn: stop=" + turn.stopReason() + " text=" + turn.text()
                + " in=" + turn.inputTokens() + " cacheRead=" + turn.cacheReadTokens());
        assertThat(body.get("model").asText()).isEqualTo("claude-opus-5-5");
        assertThat(body.get("system").get(0).has("cache_control")).isTrue();
        assertThat(body.get("tools").get(0).get("strict").asBoolean()).isTrue();
        assertThat(body.has("temperature")).isFalse();
        assertThat(body.has("tool_choice")).isFalse();
        assertThat(body.has("thinking")).isFalse();
    }

    @Test
    void retriesRateLimitsThenSucceeds() {
        server.reply(429, FakeAnthropicServer.error("rate_limit_error", "Slow down"))
                .reply(529, FakeAnthropicServer.error("overloaded_error", "Busy"))
                .reply(200, FakeAnthropicServer.message("end_turn",
                        "[{\"type\":\"text\",\"text\":\"Done\"}]"));
        long start = System.nanoTime();
        ModelPort.Turn turn = model.send(ask("Hi"));
        System.out.printf("LAB|429 then 529 then 200: answer=%s requests=%d%n", turn.text(),
                server.seen.size());
        System.out.println("LAB|retry time (ms, varies): "
                + (System.nanoTime() - start) / 1_000_000);
        assertThat(server.seen).hasSize(3);
    }

    @Test
    void translatesEachFailure() {
        record Case(int status, String type, ModelException.Kind expected, int requests) {
        }
        for (Case c : List.of(
                new Case(400, "invalid_request_error", ModelException.Kind.BAD_REQUEST, 1),
                new Case(401, "authentication_error", ModelException.Kind.AUTH, 1),
                new Case(404, "not_found_error", ModelException.Kind.NOT_FOUND, 1),
                new Case(429, "rate_limit_error", ModelException.Kind.RATE_LIMITED, 3),
                new Case(529, "overloaded_error", ModelException.Kind.OVERLOADED, 3))) {
            for (int i = 0; i < 3; i++) {
                server.reply(c.status(), FakeAnthropicServer.error(c.type(), "nope"));
            }
            assertThatThrownBy(() -> model.send(ask("Hi")))
                    .isInstanceOfSatisfying(ModelException.class, e -> {
                        System.out.printf("LAB|%d -> %-12s retryable=%-5s requests=%d%n",
                                c.status(), e.kind(), e.kind().retryable,
                                        server.seen.size());
                        assertThat(e.kind()).isEqualTo(c.expected());
                        assertThat(server.seen).hasSize(c.requests());
                    });
            server.forget();
        }
    }

    @Test
    void streamsTextAndKeepsTheWholeMessage() {
        server.replyStream(String.join("",
                FakeAnthropicServer.sseTextEvents("Your ", "order ", "arrived.")));
        List<String> fragments = new ArrayList<>();
        ModelPort.Turn turn = model.stream(ask("Hi"), fragments::add);
        System.out.println("LAB|stream fragments: " + fragments);
        System.out.println("LAB|stream turn: stop=" + turn.stopReason() + " text="
                + turn.text()
                + " out=" + turn.outputTokens());
        assertThat(fragments).containsExactly("Your ", "order ", "arrived.");
        assertThat(turn.stopReason()).isEqualTo("end_turn");
    }

    @Test
    void replaysThinkingAndToolUseBlocksUnchanged() throws Exception {
        server.reply(200, FakeAnthropicServer.message("tool_use", """
                [{"type":"thinking","thinking":"","signature":"sig-abc"},
                 {"type":"tool_use","id":"toolu_1","name":"lookup_order",
                  "input":{"order_id":"7781"}}]"""))
                .reply(200, FakeAnthropicServer.message("end_turn",
                        "[{\"type\":\"text\",\"text\":\"Delivered.\"}]"));
        ModelPort.Turn first = model.send(ask("Where is 7781?"));
        assertThat(first.calls()).hasSize(1);
        model.send(new ModelPort.Request("You are a support agent.", List.of(
                new ModelPort.UserText("Where is 7781?"),
                new ModelPort.Assistant(first),
                new ModelPort.ToolResults(List.of(new ModelPort.ToolResult(
                        "toolu_1", "delivered", false)))), List.of()));
        JsonNode messages = JSON.readTree(server.seen.get(1).body()).get("messages");
        System.out.println("LAB|second request messages: " + messages);
        JsonNode assistant = messages.get(1).get("content");
        assertThat(assistant.get(0).get("type").asText()).isEqualTo("thinking");
        assertThat(assistant.get(0).get("signature").asText()).isEqualTo("sig-abc");
        assertThat(assistant.get(1).get("type").asText()).isEqualTo("tool_use");
        assertThat(messages.get(2).get("content").get(0).get("tool_use_id").asText())
                .isEqualTo("toolu_1");
    }
}
