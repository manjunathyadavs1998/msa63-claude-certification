package course.claude;

import com.anthropic.client.AnthropicClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.StopReason;
import java.util.stream.Collectors;

/** AI03 L1: a conversation on a stateless API. This object owns all the memory. */
public final class SupportChat {

    /** What the application does next. The caller must handle every case. */
    public sealed interface Outcome {
        record Answer(String text) implements Outcome {
        }

        record Truncated(String partialText) implements Outcome {
        }

        record ToolRequested(Message message) implements Outcome {
        }

        record Declined(String explanation) implements Outcome {
        }

        record Unexpected(String stopReason) implements Outcome {
        }
    }

    private final AnthropicClient client;
    private final MessageCreateParams.Builder conversation;

    public SupportChat(AnthropicClient client, String model, String systemPrompt) {
        this.client = client;
        this.conversation = MessageCreateParams.builder()
                .model(model)
                .maxTokens(4_096L)
                .system(systemPrompt);
    }

    public Outcome send(String userText) {
        conversation.addUserMessage(userText);
        Message reply = client.messages().create(conversation.build());
        // Append the whole message, not just its text, so that later turns stay valid.
        conversation.addMessage(reply);
        return classify(reply);
    }

    /** Check stop_reason before reading content. */
    static Outcome classify(Message reply) {
        StopReason reason = reply.stopReason().orElse(null);
        if (StopReason.END_TURN.equals(reason)) {
            return new Outcome.Answer(text(reply));
        }
        if (StopReason.MAX_TOKENS.equals(reason)) {
            return new Outcome.Truncated(text(reply));
        }
        if (StopReason.TOOL_USE.equals(reason)) {
            return new Outcome.ToolRequested(reply);
        }
        if (StopReason.REFUSAL.equals(reason)) {
            String why = reply.stopDetails()
                    .flatMap(details -> details.explanation())
                    .orElse("Declined");
            return new Outcome.Declined(why);
        }
        return new Outcome.Unexpected(String.valueOf(reason));
    }

    private static String text(Message reply) {
        return reply.content().stream()
                .flatMap(block -> block.text().stream())
                .map(block -> block.text())
                .collect(Collectors.joining());
    }
}
