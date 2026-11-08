package course.rag;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Reciprocal Rank Fusion: merges ranked lists using positions, not raw scores. */
public final class RankFusion {

    private static final int K = 60;

    private RankFusion() {
    }

    public static List<String> fuse(List<List<String>> rankedLists) {
        Map<String, Double> scores = new HashMap<>();
        for (List<String> ranking : rankedLists) {
            for (int i = 0; i < ranking.size(); i++) {
                int rank = i + 1;
                scores.merge(ranking.get(i), 1.0 / (K + rank), Double::sum);
            }
        }
        Comparator<Map.Entry<String, Double>> byScore =
                Map.Entry.comparingByValue(Comparator.reverseOrder());
        return scores.entrySet().stream()
                .sorted(byScore.thenComparing(Map.Entry.comparingByKey()))
                .map(Map.Entry::getKey)
                .toList();
    }

    public static void main(String[] args) {
        List<String> vector = List.of("q2-revenue", "q1-revenue", "competitor-revenue");
        List<String> bm25 = List.of("acme-note-7", "q2-revenue", "q1-revenue");
        System.out.println("vector only = " + vector);
        System.out.println("bm25 only   = " + bm25);
        System.out.println("fused       = " + fuse(List.of(vector, bm25)));
    }
}
