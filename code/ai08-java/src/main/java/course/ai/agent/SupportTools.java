package course.ai.agent;

import course.ai.model.ModelPort.ToolDef;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Support tools, with in-memory data. Real ones would call services with narrow scopes. */
@Configuration
public class SupportTools {

    public final List<String> refundsIssued =
            new java.util.concurrent.CopyOnWriteArrayList<>();

    private static Map<String, Object> text(String description) {
        return Map.of("type", "string", "description", description);
    }

    @Bean
    AgentTool getCustomer() {
        return new AgentTool() {
            @Override
            public ToolDef definition() {
                return new ToolDef("get_customer", """
                        Confirms the identity of the customer on this case. Use this \
                        first, \
                        before any account action. Takes the email they gave.""",
                        Map.of("email", text("Exact email address")), List.of("email"));
            }

            @Override
            public String run(Map<String, Object> input, Context context) {
                boolean match = "asha@example.com".equals(input.get("email"));
                return "{\"verified\":" + match + "}";
            }
        };
    }

    @Bean
    AgentTool lookupOrder() {
        return new AgentTool() {
            @Override
            public ToolDef definition() {
                return new ToolDef("lookup_order", """
                        Looks up one order by ID. Returns status and total. Use this for a \
                        known order number. Returns found=false, not an error, if none \
                        exists.""",
                        Map.of("order_id", text("Digits only, such as 7781")),
                        List.of("order_id"));
            }

            @Override
            public String run(Map<String, Object> input, Context context) {
                String id = String.valueOf(input.get("order_id"));
                if (!id.matches("\\d+")) {
                    throw new ToolFailure(Category.VALIDATION, false,
                            "order_id must be digits only, such as 7781");
                }
                if (id.equals("5030")) {
                    throw new IllegalStateException(
                            "orders-db password rejected at 10.2.3.4");
                }
                return id.equals("7781")
                        ? "{\"found\":true,\"status\":\"DELIVERED\",\"total\":40}"
                        : "{\"found\":false}";
            }
        };
    }

    @Bean
    AgentTool processRefund() {
        return new AgentTool() {
            @Override
            public ToolDef definition() {
                return new ToolDef("process_refund", """
                        Refunds one order. Use this only after get_customer has verified \
                        the \
                        customer. Refunds over the limit are refused, so use \
                        escalate_to_human.""",
                        Map.of("order_id", text("Digits only"),
                                "amount", text("Decimal amount, such as 40.00")),
                        List.of("order_id", "amount"));
            }

            @Override
            public String run(Map<String, Object> input, Context context) {
                refundsIssued.add(input.get("order_id") + ":" + input.get("amount"));
                return "{\"refunded\":true}";
            }
        };
    }
}
