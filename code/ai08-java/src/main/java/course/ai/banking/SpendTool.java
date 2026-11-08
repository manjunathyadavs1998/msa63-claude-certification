package course.ai.banking;

import course.ai.agent.AgentTool;
import course.ai.model.ModelPort.ToolDef;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Banking totals come from code, never from the model's arithmetic. There's no customer
 * parameter: the customer comes from the authenticated context, which the model can't
 * change.
 */
public class SpendTool implements AgentTool {

    public record Txn(String id, String customerId, String category, BigDecimal amount) {
    }

    private final List<Txn> ledger;

    public SpendTool(List<Txn> ledger) {
        this.ledger = ledger;
    }

    @Override
    public ToolDef definition() {
        return new ToolDef("total_spend", """
                Adds up this customer's spending in one category. Use this for any \
                question \
                about totals, and never add figures yourself. Returns the total, and the \
                transaction IDs to cite.""",
                Map.of("category", Map.of("type", "string",
                        "description", "Such as groceries")),
                List.of("category"));
    }

    @Override
    public String run(Map<String, Object> input, Context context) {
        String category = String.valueOf(input.get("category"));
        List<Txn> mine = ledger.stream()
                .filter(txn -> txn.customerId().equals(context.customerId()))
                .filter(txn -> txn.category().equals(category))
                .toList();
        BigDecimal total = mine.stream().map(Txn::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return "{\"total\":\"" + total + "\",\"transactions\":"
                + mine.stream().map(txn -> "\"" + txn.id() + "\"").toList() + "}";
    }
}
