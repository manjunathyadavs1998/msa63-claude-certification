package course.llm;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.MessageCountTokensParams;

/** Pre-flight check: will this prompt, plus the reply we reserve, fit in the budget? */
public final class TokenBudget {

    public record Decision(long inputTokens, boolean fits) {
    }

    private final AnthropicClient client;
    private final String model;
    private final long contextWindow;

    public TokenBudget(AnthropicClient client, String model, long contextWindow) {
        this.client = client;
        this.model = model;
        this.contextWindow = contextWindow;
    }

    public Decision check(String system, String userMessage, long reservedForOutput) {
        long input = client.messages().countTokens(
                MessageCountTokensParams.builder()
                        .model(model)
                        .system(system)
                        .addUserMessage(userMessage)
                        .build())
                .inputTokens();
        return new Decision(input, input + reservedForOutput <= contextWindow);
    }

    public static void main(String[] args) {
        AnthropicClient client = AnthropicOkHttpClient.fromEnv();
        TokenBudget budget = new TokenBudget(client, "claude-opus-5-5", 1_000_000);
        Decision decision = budget.check(
                "You are a support assistant.", "Where is order 12345?", 4_096);
        System.out.println(decision);
    }
}
