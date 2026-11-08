package course.rag;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI05 L5: checks model-written SQL before it runs. A teaching sketch, not a parser:
 * production systems should use a real SQL parser and a read-only database role.
 */
public final class SqlGuard {

    private static final Pattern WRITES = Pattern.compile(
            "\\b(insert|update|delete|drop|alter|truncate|grant|create|merge)\\b");
    private static final Pattern TABLES = Pattern.compile(
            "\\b(?:from|join)\\s+([a-z_][a-z0-9_.]*)");

    private final Map<String, Set<String>> schema;

    public SqlGuard(Map<String, Set<String>> schema) {
        this.schema = schema;
    }

    public List<String> check(String sql) {
        String lower = sql.toLowerCase(Locale.ROOT).trim();
        List<String> problems = new ArrayList<>();
        if (!lower.startsWith("select") && !lower.startsWith("with")) {
            problems.add("only SELECT is allowed");
        }
        if (WRITES.matcher(lower).find()) {
            problems.add("contains a write keyword");
        }
        if (lower.indexOf(';') >= 0 && lower.indexOf(';') < lower.length() - 1) {
            problems.add("multiple statements");
        }
        Matcher tables = TABLES.matcher(lower);
        while (tables.find()) {
            if (!schema.containsKey(tables.group(1))) {
                problems.add("unknown table: " + tables.group(1));
            }
        }
        return problems;
    }

    public static void main(String[] args) {
        SqlGuard guard = new SqlGuard(Map.of(
                "orders", Set.of("id", "customer_id", "total", "created_at"),
                "customers", Set.of("id", "region")));
        for (String sql : List.of(
                "SELECT region, SUM(total) FROM orders JOIN customers "
                        + "ON orders.customer_id = customers.id GROUP BY region",
                "SELECT * FROM customer_orders_v2",
                "DELETE FROM orders",
                "SELECT 1; DROP TABLE orders")) {
            System.out.printf("%s%n  -> %s%n", sql, guard.check(sql));
        }
    }
}
