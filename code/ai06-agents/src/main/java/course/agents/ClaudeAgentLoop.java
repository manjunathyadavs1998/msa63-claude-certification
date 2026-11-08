package course.agents;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.core.JsonValue;
import com.anthropic.models.messages.ContentBlock;
import com.anthropic.models.messages.ContentBlockParam;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.MessageParam;
import com.anthropic.models.messages.StopReason;
import com.anthropic.models.messages.Tool;
import com.anthropic.models.messages.ToolResultBlockParam;
import com.anthropic.models.messages.ToolUseBlock;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * AI06 L2: the manual loop on the real SDK. Compiled against anthropic-java 3.2.0, not run,
 * because no API key is available here.
 */
public final class ClaudeAgentLoop {

    private static final int SAFETY_LIMIT = 25;

    private ClaudeAgentLoop() {
    }

    static Tool lookupOrderTool() {
        return Tool.builder()
                .name("lookup_order")
                .description("""
                        Look up one order by its ID, such as "7781". Returns status, total \
                        and delivery date. Use this for questions about a specific order, \
                        not for finding a customer's orders.""")
                .inputSchema(Tool.InputSchema.builder()
                        .properties(Tool.InputSchema.Properties.builder()
                                .putAdditionalProperty("order_id", JsonValue.from(Map.of(
                                        "type", "string",
                                        "description", "Digits only, such as 7781")))
                                .build())
                        .required(List.of("order_id"))
                        .putAdditionalProperty("additionalProperties",
                                JsonValue.from(false))
                        .build())
                .strict(true)
                .build();
    }

    static String answer(AnthropicClient client, String question) {
        MessageCreateParams.Builder params = MessageCreateParams.builder()
                .model("claude-opus-5-5")
                .maxTokens(16000L)
                .addTool(lookupOrderTool())
                .addUserMessage(question);

        for (int step = 0; step < SAFETY_LIMIT; step++) {
            Message reply = client.messages().create(params.build());
            StopReason stop = reply.stopReason().orElseThrow();

            if (stop.equals(StopReason.END_TURN)) {
                return text(reply);
            }
            if (stop.equals(StopReason.PAUSE_TURN)) {
                params.addMessage(reply); // resend as is, with no extra user message
                continue;
            }
            if (!stop.equals(StopReason.TOOL_USE)) {
                throw new IllegalStateException("Cannot continue: " + stop);
            }

            params.addMessage(reply); // the whole reply, so tool_use blocks stay valid
            List<ContentBlockParam> results = new ArrayList<>();
            for (ContentBlock block : reply.content()) {
                block.toolUse().ifPresent(call -> results.add(run(call)));
            }
            params.addMessage(MessageParam.ofUserOfContentBlockParams(results));
        }
        throw new IllegalStateException("Safety limit reached");
    }

    private static ContentBlockParam run(ToolUseBlock call) {
        boolean known = call.name().equals("lookup_order");
        return ContentBlockParam.ofToolResult(ToolResultBlockParam.builder()
                .toolUseId(call.id())
                .content(known ? "Order delivered, total $40"
                        : "Unknown tool: " + call.name())
                .isError(!known)
                .build());
    }

    private static String text(Message reply) {
        StringBuilder out = new StringBuilder();
        reply.content().forEach(b -> b.text().ifPresent(t -> out.append(t.text())));
        return out.toString();
    }

    public static void main(String[] args) {
        AnthropicClient client = AnthropicOkHttpClient.fromEnv();
        System.out.println(answer(client, "Where is order 7781?"));
    }
}
