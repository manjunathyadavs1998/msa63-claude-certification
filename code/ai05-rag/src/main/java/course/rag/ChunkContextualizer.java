package course.rag;

import com.anthropic.client.AnthropicClient;
import com.anthropic.models.messages.CacheControlEphemeral;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.StopReason;
import com.anthropic.models.messages.TextBlockParam;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/** Generates a short context for one chunk. The document is cached across its chunks. */
public final class ChunkContextualizer {

    private static final String INSTRUCTION = """
            Please give a short succinct context to situate this chunk within the \
            overall document for the purposes of improving search retrieval of the \
            chunk. Answer only with the succinct context and nothing else.""";

    private final AnthropicClient client;
    private final String model;

    public ChunkContextualizer(AnthropicClient client, String model) {
        this.client = client;
        this.model = model;
    }

    /** Empty means "index the raw chunk": a missing context must not stop ingestion. */
    public Optional<String> contextFor(String document, String chunk) {
        MessageCreateParams params = MessageCreateParams.builder()
                .model(model)
                .maxTokens(1024L)
                .systemOfTextBlockParams(List.of(cachedDocument(document)))
                .addUserMessage("<chunk>" + chunk + "</chunk>\n" + INSTRUCTION)
                .build();

        Message reply = client.messages().create(params);
        if (!reply.stopReason().filter(StopReason.END_TURN::equals).isPresent()) {
            return Optional.empty();
        }
        String text = reply.content().stream()
                .flatMap(block -> block.text().stream())
                .map(textBlock -> textBlock.text())
                .collect(Collectors.joining())
                .strip();
        return text.isEmpty() ? Optional.empty() : Optional.of(text);
    }

    private static TextBlockParam cachedDocument(String document) {
        return TextBlockParam.builder()
                .text("<document>" + document + "</document>")
                .cacheControl(CacheControlEphemeral.builder().build())
                .build();
    }
}
