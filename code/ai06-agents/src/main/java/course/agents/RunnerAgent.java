package course.agents;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.helpers.BetaToolRunner;
import com.anthropic.models.beta.messages.BetaMessage;
import com.anthropic.models.beta.messages.MessageCreateParams;
import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.function.Supplier;

/**
 * AI06 L2: the same agent using the SDK's beta tool runner, which owns the loop.
 * Compiled against anthropic-java 3.2.0, not run, because no API key is available here.
 */
public final class RunnerAgent {

    @JsonClassDescription("""
            Look up one order by its ID. Returns status, total and delivery date. \
            Use this for questions about a specific order.""")
    static class LookupOrder implements Supplier<String> {
        @JsonPropertyDescription("Digits only, such as 7781")
        public String orderId;

        @Override
        public String get() {
            return "Order " + orderId + " delivered, total $40";
        }
    }

    private RunnerAgent() {
    }

    public static void main(String[] args) {
        AnthropicClient client = AnthropicOkHttpClient.fromEnv();
        BetaToolRunner runner = client.beta().messages().toolRunner(
                MessageCreateParams.builder()
                        .model("claude-opus-5-5")
                        .maxTokens(16000L)
                        .putAdditionalHeader("anthropic-beta",
                                "structured-outputs-2025-11-13")
                        .addTool(LookupOrder.class)
                        .addUserMessage("Where is order 7781?")
                        .build());
        for (BetaMessage message : runner) {
            System.out.println(message.stopReason());
        }
    }
}
