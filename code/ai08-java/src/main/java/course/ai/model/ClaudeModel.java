package course.ai.model;

import com.anthropic.client.AnthropicClient;
import com.anthropic.core.JsonValue;
import com.anthropic.core.http.StreamResponse;
import com.anthropic.errors.AnthropicIoException;
import com.anthropic.errors.AnthropicServiceException;
import com.anthropic.errors.BadRequestException;
import com.anthropic.errors.InternalServerException;
import com.anthropic.errors.NotFoundException;
import com.anthropic.errors.RateLimitException;
import com.anthropic.errors.UnauthorizedException;
import com.anthropic.helpers.MessageAccumulator;
import com.anthropic.models.messages.CacheControlEphemeral;
import com.anthropic.models.messages.ContentBlock;
import com.anthropic.models.messages.ContentBlockParam;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.MessageParam;
import com.anthropic.models.messages.OutputConfig;
import com.anthropic.models.messages.RawMessageStreamEvent;
import com.anthropic.models.messages.TextBlockParam;
import com.anthropic.models.messages.Tool;
import com.anthropic.models.messages.ToolResultBlockParam;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** The only class that imports the Anthropic SDK. Everything else sees ModelPort. */
public class ClaudeModel implements ModelPort {

    private final AnthropicClient client;
    private final ClaudeProperties properties;

    public ClaudeModel(AnthropicClient client, ClaudeProperties properties) {
        this.client = client;
        this.properties = properties;
    }

    @Override
    public Turn send(Request request) {
        return translating(() -> toTurn(client.messages().create(params(request))));
    }

    @Override
    public Turn stream(Request request, Consumer<String> onText) {
        return translating(() -> {
            MessageAccumulator accumulator = MessageAccumulator.create();
            try (StreamResponse<RawMessageStreamEvent> events =
                    client.messages().createStreaming(params(request))) {
                events.stream().peek(accumulator::accumulate)
                        .flatMap(event -> event.contentBlockDelta().stream())
                        .flatMap(delta -> delta.delta().text().stream())
                        .forEach(text -> onText.accept(text.text()));
            }
            return toTurn(accumulator.message());
        });
    }

    MessageCreateParams params(Request request) {
        MessageCreateParams.Builder builder = MessageCreateParams.builder()
                .model(properties.model())
                .maxTokens(properties.maxTokens())
                .outputConfig(OutputConfig.builder()
                        .effort(OutputConfig.Effort.of(properties.effort()))
                        .build())
                // Stable prefix first, so that later requests read it from the cache.
                .systemOfTextBlockParams(List.of(TextBlockParam.builder()
                        .text(request.system())
                        .cacheControl(CacheControlEphemeral.builder().build())
                        .build()));
        request.tools().forEach(tool -> builder.addTool(toTool(tool)));
        for (Entry entry : request.history()) {
            switch (entry) {
                case UserText user -> builder.addUserMessage(user.text());
                case Assistant assistant
                        -> builder.addMessage((Message) assistant.turn().raw());
                case ToolResults results -> builder.addMessage(toParam(results));
            }
        }
        return builder.build();
    }

    private static MessageParam toParam(ToolResults results) {
        List<ContentBlockParam> blocks = new ArrayList<>();
        for (ToolResult result : results.results()) {
            blocks.add(ContentBlockParam.ofToolResult(ToolResultBlockParam.builder()
                    .toolUseId(result.toolUseId())
                    .content(result.content())
                    .isError(result.isError())
                    .build()));
        }
        return MessageParam.ofUserOfContentBlockParams(blocks);
    }

    private static Tool toTool(ToolDef tool) {
        Tool.InputSchema.Properties.Builder properties =
                Tool.InputSchema.Properties.builder();
        tool.properties().forEach((name, schema) ->
                properties.putAdditionalProperty(name, JsonValue.from(schema)));
        return Tool.builder()
                .name(tool.name())
                .description(tool.description())
                .strict(true)
                .inputSchema(Tool.InputSchema.builder()
                        .properties(properties.build())
                        .required(tool.required())
                        .putAdditionalProperty("additionalProperties",
                                JsonValue.from(false))
                        .build())
                .build();
    }

    private static Turn toTurn(Message message) {
        StringBuilder text = new StringBuilder();
        List<ToolCall> calls = new ArrayList<>();
        for (ContentBlock block : message.content()) {
            block.text().ifPresent(t -> text.append(t.text()));
            block.toolUse().ifPresent(use -> calls.add(new ToolCall(use.id(), use.name(),
                    input(use._input()))));
        }
        return new Turn(message.stopReason().map(Object::toString).orElse("unknown"),
                text.toString(), calls, message.usage().inputTokens(),
                message.usage().outputTokens(),
                message.usage().cacheReadInputTokens().orElse(0L), message);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> input(JsonValue value) {
        Object converted = value.convert(Map.class);
        return converted == null ? Map.of() : new LinkedHashMap<>((Map<String,
                Object>) converted);
    }

    /** Most specific first. One broad catch would lose retryable versus permanent. */
    private static <T> T translating(Supplier<T> call) {
        try {
            return call.get();
        } catch (RateLimitException e) {
            throw new ModelException(ModelException.Kind.RATE_LIMITED, "Rate limited", e);
        } catch (InternalServerException e) {
            throw new ModelException(ModelException.Kind.OVERLOADED,
                    "Provider unavailable", e);
        } catch (UnauthorizedException e) {
            throw new ModelException(ModelException.Kind.AUTH, "Check the API key", e);
        } catch (NotFoundException e) {
            throw new ModelException(ModelException.Kind.NOT_FOUND, "Check the model ID",
                    e);
        } catch (BadRequestException e) {
            throw new ModelException(ModelException.Kind.BAD_REQUEST, "Request rejected",
                    e);
        } catch (AnthropicIoException e) {
            throw new ModelException(ModelException.Kind.NETWORK, "Network failure", e);
        } catch (AnthropicServiceException e) {
            throw new ModelException(ModelException.Kind.OTHER, "Provider error", e);
        }
    }
}
