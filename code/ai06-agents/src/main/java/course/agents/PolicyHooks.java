package course.agents;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * AI06 L8: deterministic rules around tool calls, in the same shape as Agent SDK hooks.
 * These run in your application, so they hold whatever the model decides to do.
 */
public final class PolicyHooks {

    record Call(String tool, Map<String, String> input) {
    }

    record Decision(String action, String reason, String redirect) {
        static Decision allow() {
            return new Decision("ALLOW", "", "");
        }

        static Decision deny(String reason) {
            return new Decision("DENY", reason, "");
        }

        static Decision redirect(String reason, String tool) {
            return new Decision("REDIRECT", reason, tool);
        }
    }

    record Handoff(String customerId, String rootCause, String amount, String recommended,
            List<String> attempted) {
    }

    private static final double REFUND_LIMIT = 500.00;
    private static final Map<String, String> STATUS = Map.of("1", "PLACED", "2", "SHIPPED",
            "3", "DELIVERED");

    private final Set<String> verifiedCustomers = new HashSet<>();

    /** PreToolUse: runs before the tool, and can stop it. */
    Decision preToolUse(Call call) {
        if (!call.tool().equals("process_refund")) {
            return Decision.allow();
        }
        String customer = call.input().get("customer_id");
        if (!verifiedCustomers.contains(customer)) {
            return Decision.deny("Call get_customer and verify " + customer + " first");
        }
        double amount = Double.parseDouble(call.input().get("amount"));
        if (amount > REFUND_LIMIT) {
            return Decision.redirect("Refunds over $500 need a human", "escalate_to_human");
        }
        return Decision.allow();
    }

    /** PostToolUse: runs after the tool, before the model reads the result. */
    Map<String, String> postToolUse(Call call, Map<String, String> result) {
        if (call.tool().equals("get_customer") && "true".equals(result.get("verified"))) {
            verifiedCustomers.add(call.input().get("customer_id"));
        }
        Map<String, String> clean = new java.util.LinkedHashMap<>(result);
        if (clean.containsKey("updated_at")) {
            String raw = clean.get("updated_at");
            if (raw.matches("\\d+")) {
                clean.put("updated_at",
                        Instant.ofEpochSecond(Long.parseLong(raw)).toString());
            }
        }
        if (clean.containsKey("status")) {
            clean.put("status", STATUS.getOrDefault(clean.get("status"),
                    clean.get("status")));
        }
        return clean;
    }

    /** Escalation triggers from TS 5.2. Sentiment and self-reported confidence aren't
     * here. */
    static String escalationTrigger(String message, boolean policyCovers,
            int failedAttempts) {
        String lower = message.toLowerCase();
        if (lower.contains("human") || lower.contains("agent")
                || lower.contains("manager")) {
            return "customer asked for a human";
        }
        if (!policyCovers) {
            return "policy gap";
        }
        return failedAttempts >= 2 ? "no progress" : "none";
    }

    public static void main(String[] args) {
        PolicyHooks hooks = new PolicyHooks();
        Call small = new Call("process_refund", Map.of("customer_id", "c-19", "amount",
                "120.00"));
        Call large = new Call("process_refund", Map.of("customer_id", "c-19", "amount",
                "900.00"));

        System.out.println("Refund before verification: " + hooks.preToolUse(small));
        hooks.postToolUse(new Call("get_customer", Map.of("customer_id", "c-19")),
                Map.of("verified", "true"));
        System.out.println("Refund of $120 after:       " + hooks.preToolUse(small));
        System.out.println("Refund of $900 after:       " + hooks.preToolUse(large));

        Call order = new Call("lookup_order", Map.of("order_id", "7781"));
        System.out.println("Tool A raw:   {updated_at=1762300800, status=2}");
        System.out.println("Model reads:  "
                + new java.util.TreeMap<>(hooks.postToolUse(order,
                Map.of("updated_at", "1762300800", "status", "2"))));
        System.out.println("Tool B raw:   "
                + "{updated_at=2025-11-05T00:00:00Z, status=SHIPPED}");
        System.out.println("Model reads:  "
                + new java.util.TreeMap<>(hooks.postToolUse(order,
                Map.of("updated_at", "2025-11-05T00:00:00Z", "status", "SHIPPED"))));

        for (String message : List.of("I want to speak to a human",
                "This is ABSOLUTELY awful!",
                "Do you price-match competitors?")) {
            boolean covered = !message.contains("price-match");
            System.out.printf("%-34s -> %s%n", message,
                    escalationTrigger(message, covered, 0));
        }
        List<String> attempted = new ArrayList<>(List.of("verified customer",
                "checked policy"));
        System.out.println(new Handoff("c-19", "Duplicate charge on order 7781", "$900.00",
                "Approve refund, since both charges are confirmed", attempted));
    }
}
