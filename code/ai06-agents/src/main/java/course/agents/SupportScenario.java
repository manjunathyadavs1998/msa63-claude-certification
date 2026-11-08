package course.agents;

import course.agents.PolicyHooks.Call;
import course.agents.PolicyHooks.Decision;
import java.util.List;
import java.util.Map;

/**
 * AI06 L9: a scripted two-issue support case passing through the hooks. The "model" is a
 * fixed list of calls, one of them deliberately out of order. It tests the guardrails, not
 * how a model would behave.
 */
public final class SupportScenario {

    private SupportScenario() {
    }

    private static Map<String, String> tool(Call call) {
        return switch (call.tool()) {
            case "get_customer" -> Map.of("verified", "true");
            case "lookup_order" -> Map.of("status", "3", "updated_at", "1762300800");
            default -> Map.of("result", "ok");
        };
    }

    public static void main(String[] args) {
        PolicyHooks hooks = new PolicyHooks();
        List<Call> script = List.of(
                new Call("process_refund", Map.of("customer_id", "c-19", "amount",
                        "120.00")),
                new Call("get_customer", Map.of("customer_id", "c-19")),
                new Call("lookup_order", Map.of("order_id", "7781")),
                new Call("process_refund", Map.of("customer_id", "c-19", "amount",
                        "120.00")),
                new Call("lookup_order", Map.of("order_id", "8120")),
                new Call("process_refund", Map.of("customer_id", "c-19", "amount",
                        "900.00")));
        int executed = 0;
        for (Call call : script) {
            Decision decision = hooks.preToolUse(call);
            String shown = call.tool() + " " + call.input().getOrDefault("amount",
                    call.input().getOrDefault("order_id", call.input().get("customer_id")));
            if (decision.action().equals("ALLOW")) {
                Map<String, String> result = hooks.postToolUse(call, tool(call));
                executed++;
                System.out.printf("%-24s ran     %s%n", shown,
                        new java.util.TreeMap<>(result));
            } else if (decision.action().equals("DENY")) {
                System.out.printf("%-24s denied  %s%n", shown, decision.reason());
            } else {
                System.out.printf("%-24s moved   to %s: %s%n", shown, decision.redirect(),
                        decision.reason());
            }
        }
        System.out.println("Tools executed: " + executed + " of " + script.size());
        System.out.println(new PolicyHooks.Handoff("c-19", "Order 8120 charged twice",
                "$900.00",
                "Approve, since both charges are confirmed",
                List.of("verified customer", "refunded $120 on order 7781",
                        "$900 blocked by the refund limit")));
    }
}
