package course.claude;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** AI03 L6: keep exact facts outside summaries, and trim tool results early. */
public final class ContextAssembler {

    /** Exact values that must survive any summary of the conversation. */
    public record CaseFacts(String customerId, Map<String, String> orders, String stated) {
        String render() {
            StringBuilder sb = new StringBuilder("<case_facts>\n")
                    .append("customer: ").append(customerId).append('\n');
            orders.forEach((id, detail) ->
                    sb.append("order ").append(id).append(": ").append(detail)
                            .append('\n'));
            return sb.append("customer expects: ").append(stated)
                    .append("\n</case_facts>").toString();
        }
    }

    private ContextAssembler() {
    }

    /** A rough estimate for this demo only. Use count_tokens for real budgets. */
    static int estimate(String text) {
        return text.length() / 4;
    }

    static Map<String, String> sampleOrder(int number) {
        Map<String, String> order = new LinkedHashMap<>();
        order.put("order_id", "ORD-" + number);
        order.put("status", "DELIVERED");
        order.put("total", "117.42");
        order.put("currency", "USD");
        order.put("delivered_on", "2026-10-30");
        for (int i = 1; i <= 35; i++) {
            order.put("internal_field_" + i, "warehouse-routing-value-" + i);
        }
        return order;
    }

    /** Keeps only what this agent's task needs. A PostToolUse hook can do this. */
    static Map<String, String> trim(Map<String, String> toolResult, Set<String> relevant) {
        Map<String, String> kept = new LinkedHashMap<>();
        toolResult.forEach((key, value) -> {
            if (relevant.contains(key)) {
                kept.put(key, value);
            }
        });
        return kept;
    }

    public static String assemble(CaseFacts facts, String summary, List<String> recentTurns,
            String question) {
        StringBuilder sb = new StringBuilder(facts.render()).append("\n\n")
                .append("<earlier_conversation_summary>\n").append(summary)
                .append("\n</earlier_conversation_summary>\n\n<recent_turns>\n");
        recentTurns.forEach(turn -> sb.append(turn).append('\n'));
        return sb.append("</recent_turns>\n\n").append(question).toString();
    }

    public static void main(String[] args) {
        Set<String> relevant =
                Set.of("order_id", "status", "total", "currency", "delivered_on");
        Map<String, String> full = sampleOrder(1);
        Map<String, String> small = trim(full, relevant);
        int fullTokens = estimate(full.toString());
        int smallTokens = estimate(small.toString());
        System.out.printf("fields: %d -> %d%n", full.size(), small.size());
        System.out.printf("estimated tokens per lookup: %d -> %d%n",
                fullTokens, smallTokens);
        for (int lookups : new int[] {1, 10, 50}) {
            System.out.printf("%2d lookups: %,6d -> %,6d estimated tokens%n",
                    lookups, lookups * fullTokens, lookups * smallTokens);
        }

        CaseFacts facts = new CaseFacts("C-8841",
                Map.of("ORD-1", "$117.42, delivered 2026-10-30, damaged"),
                "full refund by Friday");
        String summary = "Customer reported a damaged order and asked about a refund.";
        System.out.println();
        System.out.println(assemble(facts, summary,
                List.of("user: Can you confirm the amount?"),
                "Answer the user's last message."));
    }
}
