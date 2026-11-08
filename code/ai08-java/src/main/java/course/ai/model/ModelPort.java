package course.ai.model;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/** What business code knows about the model. No SDK or framework type appears here. */
public interface ModelPort {

    record ToolDef(String name, String description, Map<String, Object> properties,
            List<String> required) {
    }

    record ToolCall(String id, String name, Map<String, Object> input) {
    }

    record ToolResult(String toolUseId, String content, boolean isError) {
    }

    /** One model reply. {@code raw} is opaque: the adapter's own copy of the whole
     * message. */
    record Turn(String stopReason, String text, List<ToolCall> calls, long inputTokens,
            long outputTokens, long cacheReadTokens, Object raw) {
    }

    sealed interface Entry permits UserText, Assistant, ToolResults {
    }

    record UserText(String text) implements Entry {
    }

    /** Replays the opaque message, so thinking and tool_use blocks survive unchanged. */
    record Assistant(Turn turn) implements Entry {
    }

    record ToolResults(List<ToolResult> results) implements Entry {
    }

    record Request(String system, List<Entry> history, List<ToolDef> tools) {
    }

    Turn send(Request request);

    /** Calls {@code onText} per text fragment, and returns the complete turn at the end. */
    Turn stream(Request request, Consumer<String> onText);
}
