package course.rag;

import com.anthropic.client.AnthropicClient;
import com.anthropic.models.messages.CitationsConfigParam;
import com.anthropic.models.messages.ContentBlockParam;
import com.anthropic.models.messages.DocumentBlockParam;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.PlainTextSource;
import com.anthropic.models.messages.TextBlockParam;
import java.util.ArrayList;
import java.util.List;

/** AI05 L4: asks a question over retrieved chunks with API-level citations enabled. */
public final class CitedAnswers {

    public record Chunk(String title, String text) {
    }

    private final AnthropicClient client;
    private final String model;

    public CitedAnswers(AnthropicClient client, String model) {
        this.client = client;
        this.model = model;
    }

    public Message answer(List<Chunk> chunks, String question) {
        List<ContentBlockParam> blocks = new ArrayList<>();
        for (Chunk chunk : chunks) {
            // Citations must be enabled on all documents in a request, or on none.
            blocks.add(ContentBlockParam.ofDocument(DocumentBlockParam.builder()
                    .source(PlainTextSource.builder().data(chunk.text()).build())
                    .title(chunk.title())
                    .citations(CitationsConfigParam.builder().enabled(true).build())
                    .build()));
        }
        blocks.add(ContentBlockParam.ofText(TextBlockParam.builder()
                .text(question + " Answer only from the documents. If they don't "
                        + "contain the answer, say so.")
                .build()));
        return client.messages().create(MessageCreateParams.builder()
                .model(model)
                .maxTokens(4_096L)
                .addUserMessageOfContentBlockParams(blocks)
                .build());
    }
}
