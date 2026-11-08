package course.claude;

import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.OutputConfig;
import com.anthropic.models.messages.ThinkingConfigAdaptive;

/** AI03 L4: effort and thinking, set explicitly for each route. */
public final class ModelRequests {

    private ModelRequests() {
    }

    /** Effort lives inside output_config. Opus 5.5 defaults to medium, so be explicit. */
    public static MessageCreateParams hardReasoning(String prompt) {
        return MessageCreateParams.builder()
                .model("claude-opus-5-5")
                .maxTokens(16_000L)
                .thinking(ThinkingConfigAdaptive.builder()
                        .display(ThinkingConfigAdaptive.Display.SUMMARIZED)
                        .build())
                .outputConfig(OutputConfig.builder()
                        .effort(OutputConfig.Effort.HIGH)
                        .build())
                .addUserMessage(prompt)
                .build();
    }

    public static MessageCreateParams simpleClassification(String ticket) {
        return MessageCreateParams.builder()
                .model("claude-opus-5-5")
                .maxTokens(256L)
                .outputConfig(OutputConfig.builder()
                        .effort(OutputConfig.Effort.LOW)
                        .build())
                .addUserMessage(
                        "Classify this ticket as billing, delivery or other:\n" + ticket)
                .build();
    }
}
