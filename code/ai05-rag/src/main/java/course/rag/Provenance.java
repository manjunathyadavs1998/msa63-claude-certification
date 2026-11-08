package course.rag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * AI05 L4: structured findings keep each claim tied to its source, and a merge
 * annotates conflicts instead of silently picking a value. Data is invented.
 */
public final class Provenance {

    /** Content and metadata stay separate, so that summarizing can't lose the source. */
    public record Finding(String claimKey, String value, String excerpt, String sourceUrl,
            String published) {
    }

    public record Merged(String claimKey, String status, List<Finding> evidence) {
    }

    private Provenance() {
    }

    /** A citation is valid only if its excerpt really appears in the document it names. */
    static boolean excerptVerified(Finding finding, Map<String, String> corpus) {
        String source = corpus.get(finding.sourceUrl());
        return source != null && source.contains(finding.excerpt());
    }

    static List<Merged> merge(List<Finding> findings) {
        Map<String, List<Finding>> byClaim = new java.util.TreeMap<>();
        for (Finding finding : findings) {
            byClaim.computeIfAbsent(finding.claimKey(), k -> new ArrayList<>())
                    .add(finding);
        }
        List<Merged> merged = new ArrayList<>();
        byClaim.forEach((claim, evidence) -> {
            long distinctValues = evidence.stream()
                    .map(Finding::value).distinct().count();
            long distinctDates = evidence.stream()
                    .map(Finding::published).distinct().count();
            String status;
            if (distinctValues == 1) {
                status = evidence.size() > 1 ? "WELL_SUPPORTED" : "SINGLE_SOURCE";
            } else {
                status = distinctDates > 1 ? "DIFFERS_BY_DATE" : "CONTESTED";
            }
            merged.add(new Merged(claim, status, evidence));
        });
        return merged;
    }

    public static void main(String[] args) {
        Map<String, String> corpus = Map.of(
                "https://example.org/a",
                "The market reached $4.1B in 2023, per our survey.",
                "https://example.org/b", "We estimate the market at $5.6B in 2025.",
                "https://example.org/c", "Our 2025 survey puts the market at $5.6B.",
                "https://example.org/d", "Adoption grew 31% in 2025.",
                "https://example.org/e", "Adoption grew 18% in 2025, by our count.");
        List<Finding> findings = List.of(
                new Finding("market_size", "$4.1B", "The market reached $4.1B in 2023",
                        "https://example.org/a", "2023"),
                new Finding("market_size", "$5.6B", "the market at $5.6B in 2025",
                        "https://example.org/b", "2025"),
                new Finding("adoption_growth", "31%", "Adoption grew 31% in 2025",
                        "https://example.org/d", "2025"),
                new Finding("adoption_growth", "18%", "Adoption grew 18% in 2025",
                        "https://example.org/e", "2025"),
                new Finding("survey_value", "$5.6B", "puts the market at $5.6B",
                        "https://example.org/c", "2025"),
                new Finding("survey_value", "$5.6B", "the market at $5.6B",
                        "https://example.org/b", "2025"));
        for (Merged merged : merge(findings)) {
            System.out.printf("%-16s %-16s %s%n", merged.claimKey(), merged.status(),
                    merged.evidence().stream()
                            .map(f -> f.value() + " (" + f.published() + ")").toList());
        }
        Finding fabricated = new Finding("market_size", "$9B", "The market reached $9B",
                "https://example.org/a", "2023");
        System.out.println("real excerpt verified: "
                + excerptVerified(findings.get(0), corpus));
        System.out.println("invented excerpt verified: "
                + excerptVerified(fabricated, corpus));
    }
}
