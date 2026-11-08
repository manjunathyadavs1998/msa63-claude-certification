package course.claude;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** AI03 L3: validate, retry with specific feedback, and know when a retry can't help. */
public final class Extraction {

    public record LineItem(String description, BigDecimal amount) {
    }

    public enum Category { GOODS, SERVICES, OTHER, UNCLEAR }

    /** Nullable fields can honestly say "not in the document". */
    public record Invoice(
            String invoiceNumber,
            String purchaseOrder,
            String currency,
            BigDecimal statedTotal,
            List<LineItem> items,
            Category category,
            String categoryDetail) {
    }

    public enum Kind { FORMAT, SEMANTIC, ABSENT }

    public record Problem(Kind kind, String message) {
        boolean retryable() {
            return kind != Kind.ABSENT;
        }
    }

    public interface Extractor {
        Invoice extract(String document, String feedback);
    }

    public sealed interface Result {
        record Accepted(Invoice invoice, int attempts) implements Result {
        }

        record NeedsReview(Invoice invoice, List<Problem> problems, int attempts)
                implements Result {
        }
    }

    /** A schema can't check these. Only code that knows the business rules can. */
    static List<Problem> validate(Invoice invoice) {
        List<Problem> problems = new ArrayList<>();
        if (invoice.currency() == null || !invoice.currency().matches("[A-Z]{3}")) {
            problems.add(new Problem(Kind.FORMAT,
                    "currency must be a 3-letter ISO code, got: " + invoice.currency()));
        }
        if (invoice.statedTotal() == null) {
            problems.add(new Problem(Kind.ABSENT, "no stated total in the document"));
        } else {
            BigDecimal sum = invoice.items().stream()
                    .map(LineItem::amount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (sum.compareTo(invoice.statedTotal()) != 0) {
                problems.add(new Problem(Kind.SEMANTIC, "line items sum to " + sum
                        + " but the stated total is " + invoice.statedTotal()));
            }
        }
        if (invoice.category() == Category.OTHER && invoice.categoryDetail() == null) {
            problems.add(new Problem(Kind.FORMAT, "category OTHER needs categoryDetail"));
        }
        return problems;
    }

    static String feedback(Invoice failed, List<Problem> problems) {
        StringBuilder sb = new StringBuilder("Your previous extraction was:\n")
                .append(failed).append("\nIt failed these checks:\n");
        problems.forEach(p -> sb.append("- ").append(p.message()).append('\n'));
        return sb.append("Re-read the document and correct only these fields. If a value ")
                .append("isn't in the document, use null.").toString();
    }

    public static Result run(Extractor extractor, String document, int maxAttempts) {
        String feedback = null;
        Invoice invoice = null;
        List<Problem> problems = List.of();
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            invoice = extractor.extract(document, feedback);
            problems = validate(invoice);
            if (problems.isEmpty()) {
                return new Result.Accepted(invoice, attempt);
            }
            // Absent information can't be found by asking again.
            if (problems.stream().noneMatch(Problem::retryable)) {
                return new Result.NeedsReview(invoice, problems, attempt);
            }
            feedback = feedback(invoice, problems);
        }
        return new Result.NeedsReview(invoice, problems, maxAttempts);
    }

    /** Replays canned model answers, so that the loop's logic runs without an API key. */
    static Extractor scripted(Invoice... answers) {
        int[] next = {0};
        return (document, feedback) -> {
            if (feedback != null) {
                System.out.println("  feedback sent: " + feedback.lines()
                        .filter(line -> line.startsWith("- ")).toList());
            }
            return answers[Math.min(next[0]++, answers.length - 1)];
        };
    }

    static Invoice invoice(String currency, String total, String... amounts) {
        List<LineItem> items = new ArrayList<>();
        for (String amount : amounts) {
            items.add(new LineItem("item", new BigDecimal(amount)));
        }
        BigDecimal stated = total == null ? null : new BigDecimal(total);
        return new Invoice("INV-1", null, currency, stated, items, Category.GOODS, null);
    }

    static void show(String name, Result result) {
        String summary = switch (result) {
            case Result.Accepted a -> "ACCEPTED after " + a.attempts() + " attempt(s)";
            case Result.NeedsReview r -> "HUMAN REVIEW after " + r.attempts()
                    + " attempt(s): " + r.problems().stream().map(Problem::kind).toList();
        };
        System.out.println(name + ": " + summary);
    }

    public static void main(String[] args) {
        System.out.println("1. Format error, fixable");
        show("   result", run(scripted(
                invoice("dollars", "150.00", "100.00", "50.00"),
                invoice("USD", "150.00", "100.00", "50.00")), "doc", 3));
        System.out.println("2. Purchase order absent, and the field is nullable");
        show("   result", run(scripted(
                invoice("EUR", "80.00", "80.00")), "doc", 3));
        System.out.println("3. Total missing from the source");
        show("   result", run(scripted(
                invoice("USD", null, "80.00")), "doc", 3));
        System.out.println("4. Source really does contradict itself");
        show("   result", run(scripted(
                invoice("USD", "200.00", "100.00", "50.00")), "doc", 3));
    }
}
