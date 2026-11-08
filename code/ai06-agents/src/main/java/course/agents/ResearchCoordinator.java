package course.agents;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * AI06 L6 and L10: the coordinator's control flow with simulated subagents. Each subagent
 * sleeps 300 ms in place of real work, so timings show concurrency only. Findings are
 * invented. No model is called.
 */
public final class ResearchCoordinator {

    record Finding(String claim, String source, String published) {
    }

    record SubResult(String aspect, String status, List<Finding> findings,
            String attempted) {
    }

    private static final int SIMULATED_MILLIS = 300;
    private static final int MAX_ROUNDS = 3;

    private ResearchCoordinator() {
    }

    /** Subagents inherit nothing, so everything they need goes in this one string. */
    static String subagentPrompt(String goal, String aspect, List<Finding> known) {
        return """
                Goal: %s
                Your part: %s only. Others cover the other aspects.
                Already known, so don't repeat it: %s
                Return: JSON findings with claim, source URL and publication date.
                Stop when: you have 3 independent sources, or after 10 searches.
                If a source fails: say which, and return what you have."""
                .formatted(goal, aspect, known.isEmpty() ? "nothing yet" : known);
    }

    private static SubResult subagent(String goal, String aspect, List<Finding> known) {
        String prompt = subagentPrompt(goal, aspect, known);
        try {
            Thread.sleep(SIMULATED_MILLIS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (aspect.equals("billing") && prompt != null) {
            return new SubResult(aspect, "PARTIAL",
                    List.of(new Finding("Coding audits take less time",
                            "https://example.org/a",
                            "2025")),
                                    "journal search worked; payer portal timed out twice");
        }
        return new SubResult(aspect, "OK",
                List.of(new Finding(aspect + " finding", "https://example.org/" + aspect,
                        "2026")),
                "web and journal search");
    }

    static List<SubResult> inParallel(String goal, List<String> aspects,
            List<Finding> known)
            throws Exception {
        try (ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<SubResult>> running = new ArrayList<>();
            for (String aspect : aspects) {
                running.add(pool.submit(() -> subagent(goal, aspect, known)));
            }
            List<SubResult> results = new ArrayList<>();
            for (Future<SubResult> future : running) {
                results.add(future.get());
            }
            return results;
        }
    }

    public static void main(String[] args) throws Exception {
        String goal = "How is AI changing hospital operations?";
        Set<String> required = new LinkedHashSet<>(
                List.of("radiology", "triage", "billing", "nursing"));
        List<String> plan = new ArrayList<>(List.of("radiology", "triage"));

        System.out.println(subagentPrompt(goal, "triage", List.of()));
        System.out.println();

        List<SubResult> all = new ArrayList<>();
        Set<String> covered = new LinkedHashSet<>();
        Instant start = Instant.now();
        for (int round = 1; round <= MAX_ROUNDS && !plan.isEmpty(); round++) {
            List<Finding> known = all.stream().flatMap(r -> r.findings().stream()).toList();
            List<SubResult> results = inParallel(goal, plan, known);
            all.addAll(results);
            results.forEach(r -> covered.add(r.aspect()));
            List<String> gaps = required.stream().filter(a ->
                    !covered.contains(a)).toList();
            System.out.printf("Round %d ran %s, gaps now %s%n", round, plan, gaps);
            plan = new ArrayList<>(gaps);
        }
        long parallelMillis = Duration.between(start, Instant.now()).toMillis();
        System.out.println();
        for (SubResult r : all) {
            System.out.printf("%-10s %-8s %d finding(s), tried: %s%n", r.aspect(),
                    r.status(),
                    r.findings().size(), r.attempted());
        }
        System.out.println();
        System.out.println("Coverage notes for the report:");
        all.stream().filter(r -> !r.status().equals("OK")).forEach(r -> System.out.println(
                "  " + r.aspect() + " is partial: " + r.attempted()));
        System.out.printf("%nSubagents: %d. Sequential would sleep %d ms. This run took "
                + "about %d ms (rounded to 100).%n", all.size(),
                        all.size() * SIMULATED_MILLIS,
                Math.round(parallelMillis / 100.0) * 100);
    }
}
