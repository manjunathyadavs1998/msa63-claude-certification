package course.ai.agent;

import course.ai.model.ModelPort;
import java.util.Map;

/** A tool the model can ask for. {@code context} is ours, and the model can't see it. */
public interface AgentTool {

    record Context(String caseId, String customerId) {
    }

    enum Category { TRANSIENT, VALIDATION, BUSINESS, PERMISSION }

    /** Thrown by tools. Only this message reaches the model, so keep it safe to share. */
    class ToolFailure extends RuntimeException {
        public final Category category;
        public final boolean retryable;

        public ToolFailure(Category category, boolean retryable, String safeMessage) {
            super(safeMessage);
            this.category = category;
            this.retryable = retryable;
        }
    }

    ModelPort.ToolDef definition();

    String run(Map<String, Object> input, Context context);
}
