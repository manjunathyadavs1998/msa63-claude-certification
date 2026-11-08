package course.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import course.ai.model.ClaudeModel;
import course.ai.model.ClaudeProperties;
import course.ai.model.ModelException;
import course.ai.model.ModelPort;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * AI10 L1: a spend-cap 429 has the same status and type as a rate limit, per the docs. The
 * body below is copied from the rate limits page, and served by a local fake.
 */
class SpendCapTest {

    private static final String SPEND_CAP = """
            {"type":"error","error":{"type":"rate_limit_error","message":"You have reached \
            your API usage limits","details":{"error_code":\
            "enforced_spend_limit_reached"}},\
            "request_id":"req_018EeWyXxfu5pfWkrYcMdjWG"}""";

    @Test
    void theSdkRetriesASpendCapAndOurMappingCallsItRetryable() throws Exception {
        try (FakeAnthropicServer server = new FakeAnthropicServer()) {
            ClaudeProperties properties = new ClaudeProperties("claude-opus-5-5", 16000,
                    Duration.ofSeconds(5), 2, "medium", server.url(), "test-key");
            AnthropicClient client = AnthropicOkHttpClient.builder().apiKey("test-key")
                    .baseUrl(server.url()).timeout(properties.timeout())
                    .maxRetries(properties.maxRetries()).build();
            for (int i = 0; i < 3; i++) {
                server.reply(429, SPEND_CAP);
            }
            ClaudeModel model = new ClaudeModel(client, properties);
            ModelPort.Request request = new ModelPort.Request("You help.",
                    List.of(new ModelPort.UserText("Hi")), List.of());
            assertThatThrownBy(() -> model.send(request))
                    .isInstanceOfSatisfying(ModelException.class, e -> {
                        System.out.printf("LAB|spend cap: requests=%d kind=%s retryable=%s"
                                + " sentToCaller=%d%n", server.seen.size(), e.kind(),
                                e.kind().retryable, e.kind().httpStatus);
                        assertThat(server.seen).hasSize(3);
                        assertThat(e.kind().retryable).isTrue();
                    });
            client.close();
        }
    }
}
