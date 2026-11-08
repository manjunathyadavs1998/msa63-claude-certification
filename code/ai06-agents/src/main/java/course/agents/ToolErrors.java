package course.agents;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;

/**
 * AI06 L5: what structured tool errors let a caller do. Tools are scripted to fail in set
 * ways, and the numbers count calls in this script, not any real service's behavior.
 */
public final class ToolErrors {

    enum Category { TRANSIENT, VALIDATION, BUSINESS, PERMISSION }

    record ToolResult(boolean isError, Category category, boolean retryable,
            String message) {
        static ToolResult ok(String text) {
            return new ToolResult(false, null, false, text);
        }

        static ToolResult fail(Category category, boolean retryable, String message) {
            return new ToolResult(true, category, retryable, message);
        }

        /** The JSON that goes in the MCP result's content, alongside isError: true. */
        String wire() {
            return String.format(
                    "{\"errorCategory\":\"%s\",\"isRetryable\":%s,\"message\":\"%s\"}",
                    category.name().toLowerCase(), retryable, message);
        }
    }

    record Report(String action, int calls, String detail) {
    }

    private static final int MAX_ATTEMPTS = 3;

    private ToolErrors() {
    }

    static Report structured(IntFunction<ToolResult> tool) {
        int calls = 0;
        ToolResult result = null;
        while (calls < MAX_ATTEMPTS) {
            result = tool.apply(calls);
            calls++;
            if (!result.isError()) {
                return new Report("use result", calls, result.message());
            }
            if (!result.retryable()) {
                break;
            }
        }
        return new Report(actionFor(result), calls, result.message());
    }

    private static String actionFor(ToolResult result) {
        return switch (result.category()) {
            case TRANSIENT -> "report to coordinator, with what was tried";
            case VALIDATION -> "correct the input";
            case BUSINESS -> "explain to the customer";
            case PERMISSION -> "escalate";
        };
    }

    /** Every failure looks the same, so the only available move is to try again. */
    static Report generic(IntFunction<ToolResult> tool) {
        int calls = 0;
        ToolResult result = null;
        while (calls < MAX_ATTEMPTS) {
            result = tool.apply(calls);
            calls++;
            if (!result.isError()) {
                return new Report("use result", calls, result.message());
            }
        }
        return new Report("give up", calls, "Operation failed");
    }

    public static void main(String[] args) {
        record Scenario(String name, IntFunction<ToolResult> tool) {
        }
        List<Scenario> scenarios = new ArrayList<>(List.of(
                new Scenario("timeout, then fine", n -> n < 2
                        ? ToolResult.fail(Category.TRANSIENT, true, "Billing timed out")
                        : ToolResult.ok("Invoice INV-44")),
                new Scenario("service stays down", n ->
                        ToolResult.fail(Category.TRANSIENT, true, "Billing unavailable")),
                new Scenario("bad date format", n -> ToolResult.fail(Category.VALIDATION,
                        false,
                        "from_date must be YYYY-MM-DD, got 03/11/2026")),
                new Scenario("refund over policy", n -> ToolResult.fail(Category.BUSINESS,
                        false,
                        "Refunds over $500 need a manager")),
                new Scenario("not authorized", n -> ToolResult.fail(Category.PERMISSION,
                        false,
                        "This agent can't read payroll")),
                new Scenario("no matching orders", n -> ToolResult.ok("0 orders found"))));
        int structuredCalls = 0;
        int genericCalls = 0;
        System.out.printf("%-20s %-9s %-8s %s%n", "Scenario", "generic", "typed",
                "typed action");
        for (Scenario s : scenarios) {
            Report typed = structured(s.tool());
            Report plain = generic(s.tool());
            structuredCalls += typed.calls();
            genericCalls += plain.calls();
            System.out.printf("%-20s %-9d %-8d %s%n", s.name(), plain.calls(),
                    typed.calls(),
                    typed.action());
        }
        System.out.printf("%-20s %-9d %-8d%n", "Total tool calls", genericCalls,
                structuredCalls);
        System.out.println(ToolResult.fail(Category.BUSINESS, false,
                "Refunds over $500 need a manager").wire());
    }
}
