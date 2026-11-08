package course.ai.security;

import course.ai.agent.AgentProperties;
import course.ai.agent.AgentTool;
import course.ai.agent.RefundPolicy;
import course.ai.model.ModelPort.ToolCall;
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI09 L3: treats the model's refund arguments as hostile input. Found by attacking
 * RefundPolicy, which checks identity and a limit but not whose order this is. An in-memory
 * sketch: the real checks belong in the payment service, with a database constraint.
 */
public class OrderBoundRefundPolicy extends RefundPolicy {

    public record Order(String ownerId, BigDecimal total) {
    }

    private final Map<String, Order> orders;
    private final Map<String, BigDecimal> refunded = new ConcurrentHashMap<>();

    public OrderBoundRefundPolicy(AgentProperties limits, Map<String, Order> orders) {
        super(limits);
        this.orders = orders;
    }

    @Override
    public Decision before(ToolCall call, AgentTool.Context context) {
        if (!call.name().equals("process_refund")) {
            return super.before(call, context);
        }
        BigDecimal amount;
        try {
            amount = new BigDecimal(String.valueOf(call.input().get("amount")));
        } catch (NumberFormatException e) {
            return new Deny("The amount must be a number, such as 40.00.");
        }
        if (amount.signum() <= 0) {
            return new Deny("The amount must be above zero.");
        }
        String orderId = String.valueOf(call.input().get("order_id"));
        Order order = orders.get(orderId);
        // The same reply for "missing" and "someone else's", so nothing can be probed.
        if (order == null || !order.ownerId().equals(context.customerId())) {
            return new Deny("That order isn't on this customer's account.");
        }
        Decision base = super.before(call, context);
        return base instanceof Deny ? base : reserve(orderId, order, amount);
    }

    /** Atomic, because the agent runs a turn's tool calls in parallel. */
    private Decision reserve(String orderId, Order order, BigDecimal amount) {
        boolean[] fits = new boolean[1];
        refunded.compute(orderId, (id, sum) -> {
            BigDecimal soFar = sum == null ? BigDecimal.ZERO : sum;
            fits[0] = soFar.add(amount).compareTo(order.total()) <= 0;
            return fits[0] ? soFar.add(amount) : soFar;
        });
        return fits[0] ? new Allow()
                : new Deny("That's more than is left to refund on this order.");
    }
}
