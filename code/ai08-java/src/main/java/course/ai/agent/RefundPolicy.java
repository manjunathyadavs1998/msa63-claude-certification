package course.ai.agent;

import course.ai.model.ModelPort.ToolCall;
import java.math.BigDecimal;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/** Rules that must hold whatever the model decides. Keep them in the backend service too.
 * */
@Component
public class RefundPolicy {

    public sealed interface Decision permits Allow, Deny {
    }

    public record Allow() implements Decision {
    }

    public record Deny(String reason) implements Decision {
    }

    private final Set<String> verified = ConcurrentHashMap.newKeySet();
    private final BigDecimal limit;

    public RefundPolicy(AgentProperties properties) {
        this.limit = properties.refundLimit();
    }

    public Decision before(ToolCall call, AgentTool.Context context) {
        if (!call.name().equals("process_refund")) {
            return new Allow();
        }
        if (!verified.contains(context.caseId())) {
            return new Deny("Identity isn't verified. Call get_customer first.");
        }
        BigDecimal amount = new BigDecimal(String.valueOf(call.input().get("amount")));
        if (amount.compareTo(limit) > 0) {
            return new Deny("Refunds over " + limit
                    + " need a human. Use escalate_to_human.");
        }
        return new Allow();
    }

    /** Set from the tool's real result in code, and never from what the model claims. */
    public void after(ToolCall call, String result, AgentTool.Context context) {
        if (call.name().equals("get_customer") && result.contains("\"verified\":true")) {
            verified.add(context.caseId());
        }
    }
}
