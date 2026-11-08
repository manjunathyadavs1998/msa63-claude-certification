package course.claude;

import com.anthropic.client.AnthropicClient;
import com.anthropic.core.JsonValue;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.StructuredMessageCreateParams;
import com.anthropic.models.messages.Tool;
import com.anthropic.models.messages.ToolChoiceTool;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** AI03 L3: the two ways to get structured output from Claude. */
public final class ExtractionClient {

    public record ExtractedOrder(String orderId, String deliveryDate, String notes) {
    }

    private final AnthropicClient client;
    private final String model;

    public ExtractionClient(AnthropicClient client, String model) {
        this.client = client;
        this.model = model;
    }

    /** Current recommended route: the schema is derived from the record. */
    public Optional<ExtractedOrder> extract(String document) {
        StructuredMessageCreateParams<ExtractedOrder> params = MessageCreateParams.builder()
                .model(model)
                .maxTokens(4_096L)
                .outputConfig(ExtractedOrder.class)
                .addUserMessage("<document>" + document + "</document>\nExtract the order.")
                .build();
        return client.messages().create(params).content().stream()
                .flatMap(block -> block.text().stream())
                .map(typed -> typed.text())
                .findFirst();
    }

    /** The exam's route: an extraction tool, forced so that Claude must call it. */
    public MessageCreateParams forcedTool(String olderModel, String document) {
        Tool tool = Tool.builder()
                .name("record_order")
                .description("Record the order fields found in the document.")
                .strict(true)
                .inputSchema(Tool.InputSchema.builder()
                        .properties(Tool.InputSchema.Properties.builder()
                                .putAdditionalProperty("order_id",
                                        JsonValue.from(Map.of("type", "string")))
                                .putAdditionalProperty("delivery_date",
                                        JsonValue.from(Map.of(
                                                "type", List.of("string", "null"))))
                                .build())
                        .required(List.of("order_id", "delivery_date"))
                        .putAdditionalProperty("additionalProperties",
                                JsonValue.from(false))
                        .build())
                .build();
        return MessageCreateParams.builder()
                .model(olderModel)
                .maxTokens(4_096L)
                .addTool(tool)
                // Returns a 400 on Opus 5.5, Sonnet 5.5 and Fable 5.1.
                .toolChoice(ToolChoiceTool.builder().name("record_order").build())
                .addUserMessage("<document>" + document + "</document>")
                .build();
    }
}
