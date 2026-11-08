package course.rag;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AI05 L6: scores the two halves of RAG separately, by segment, and names which to fix.
 * Results are scripted. This tests the harness, not any particular model or retriever.
 */
public final class RagEval {

    /** One graded case: was the right chunk retrieved, and did the answer stay faithful? */
    record Case(String type, boolean retrievedRightChunk, boolean answerFaithful,
            boolean answerCorrect) {
    }

    private RagEval() {
    }

    static List<Case> cases() {
        List<Case> cases = new java.util.ArrayList<>();
        add(cases, "policy lookup", 40, 37, 36, 35);
        add(cases, "error code", 20, 10, 10, 10);
        add(cases, "multi-document", 20, 16, 11, 10);
        add(cases, "no answer exists", 20, 0, 12, 12);
        return cases;
    }

    private static void add(List<Case> cases, String type, int total, int retrieved,
            int faithful, int correct) {
        for (int i = 0; i < total; i++) {
            cases.add(new Case(type, i < retrieved, i < faithful, i < correct));
        }
    }

    static String diagnose(double recall, double faithful) {
        if (recall < 0.8 && faithful >= 0.8) {
            return "fix retrieval";
        }
        if (recall >= 0.8 && faithful < 0.8) {
            return "fix generation";
        }
        return recall < 0.8 ? "fix ingestion or retrieval first" : "healthy";
    }

    public static void main(String[] args) {
        List<Case> all = cases();
        System.out.printf("%-18s %5s %7s %9s %8s  %s%n",
                "Segment", "cases", "recall", "faithful", "correct", "diagnosis");
        report("ALL", all);
        Map<String, List<Case>> byType = new LinkedHashMap<>();
        all.forEach(c -> byType
                .computeIfAbsent(c.type(), t -> new java.util.ArrayList<>()).add(c));
        byType.forEach(RagEval::report);
    }

    static void report(String name, List<Case> cases) {
        double n = cases.size();
        double recall = cases.stream().filter(Case::retrievedRightChunk).count() / n;
        double faithful = cases.stream().filter(Case::answerFaithful).count() / n;
        double correct = cases.stream().filter(Case::answerCorrect).count() / n;
        String diagnosis = name.equals("no answer exists")
                ? "abstain rate is the metric"
                : diagnose(recall, faithful);
        System.out.printf("%-18s %5.0f %7.2f %9.2f %8.2f  %s%n",
                name, n, recall, faithful, correct, diagnosis);
    }
}
