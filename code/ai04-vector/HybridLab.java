import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * AI04 L3: BM25, a stand-in embedder, fusion methods and retrieval metrics.
 * The "embedder" maps words onto hand-written concepts. It isn't a real model,
 * and the corpus and labels are invented, so only the behaviors carry over.
 */
public class HybridLab {

    record Doc(String id, String text) {
    }

    record Query(String text, String relevant) {
    }

    static final List<Doc> DOCS = List.of(
            new Doc("payment-timeouts",
                    "ERR-4012 gateway timeout during card capture retry"),
            new Doc("refund-policy",
                    "reimbursement rules for damaged goods and returned orders"),
            new Doc("refund-delays",
                    "why a reimbursement can take several days to reach a card"),
            new Doc("login-lockout",
                    "account locked after repeated failed sign in attempts"),
            new Doc("password-reset", "steps to recover account access with a reset link"),
            new Doc("shipping-delays", "courier delays and late parcel delivery estimates"),
            new Doc("address-change", "update the delivery address before dispatch"),
            new Doc("kafka-lag", "ERR-7781 consumer lag on the orders topic"),
            new Doc("db-failover",
                    "primary database failover and replica promotion runbook"),
            new Doc("cache-stampede",
                    "redis cache expiry causes a load spike on the database"),
            new Doc("rate-limits", "ERR-4290 too many requests returned by the public API"),
            new Doc("invoice-copy", "download a copy of an invoice from the billing page"));

    static final List<Query> QUERIES = List.of(
            new Query("ERR-4012", "payment-timeouts"),
            new Query("ERR-7781", "kafka-lag"),
            new Query("ERR-4290", "rate-limits"),
            new Query("money back for a broken item", "refund-policy"),
            new Query("cannot get into my profile", "login-lockout"),
            new Query("my package is running behind", "shipping-delays"),
            new Query("forgot my credentials", "password-reset"),
            new Query("replica promotion", "db-failover"),
            new Query("refund taking too long to reach my card", "refund-delays"),
            new Query("database overloaded when cache entries expire", "cache-stampede"));

    /** Stand-in for an embedding model: synonyms share a concept. */
    static final Map<String, String> CONCEPTS = Map.ofEntries(
            Map.entry("money", "refund"), Map.entry("refund", "refund"),
            Map.entry("reimbursement", "refund"), Map.entry("back", "refund"),
            Map.entry("broken", "damage"), Map.entry("damaged", "damage"),
            Map.entry("item", "goods"), Map.entry("goods", "goods"),
            Map.entry("orders", "goods"), Map.entry("profile", "account"),
            Map.entry("account", "account"), Map.entry("get", "access"),
            Map.entry("access", "access"), Map.entry("locked", "access"),
            Map.entry("sign", "access"), Map.entry("package", "parcel"),
            Map.entry("parcel", "parcel"), Map.entry("running", "late"),
            Map.entry("behind", "late"), Map.entry("late", "late"),
            Map.entry("delays", "late"), Map.entry("forgot", "recover"),
            Map.entry("recover", "recover"), Map.entry("credentials", "password"),
            Map.entry("reset", "password"), Map.entry("reach", "reach"),
            Map.entry("long", "late"), Map.entry("card", "card"),
            Map.entry("database", "database"), Map.entry("overloaded", "spike"),
            Map.entry("spike", "spike"), Map.entry("cache", "cache"),
            Map.entry("redis", "cache"), Map.entry("expire", "expiry"),
            Map.entry("expiry", "expiry"), Map.entry("replica", "database"),
            Map.entry("failover", "database"), Map.entry("consumer", "queue"));

    static List<String> words(String text) {
        return List.of(text.toLowerCase().split("[^a-z0-9-]+"));
    }

    static Map<String, Integer> counts(List<String> tokens) {
        Map<String, Integer> out = new HashMap<>();
        tokens.forEach(t -> out.merge(t, 1, Integer::sum));
        return out;
    }

    /** BM25 with the usual defaults, k1 = 1.2 and b = 0.75. */
    static Map<String, Double> bm25(String query) {
        double avgLength = DOCS.stream().mapToInt(d -> words(d.text()).size())
                .average().orElse(1);
        Map<String, Double> scores = new HashMap<>();
        for (Doc doc : DOCS) {
            List<String> tokens = words(doc.text());
            Map<String, Integer> tf = counts(tokens);
            double score = 0;
            for (String term : words(query)) {
                long df = DOCS.stream().filter(d -> words(d.text()).contains(term)).count();
                if (df == 0 || !tf.containsKey(term)) {
                    continue;
                }
                double idf = Math.log(1 + (DOCS.size() - df + 0.5) / (df + 0.5));
                double f = tf.get(term);
                double lengthNorm = 0.25 + 0.75 * tokens.size() / avgLength;
                score += idf * f * 2.2 / (f + 1.2 * lengthNorm);
            }
            if (score > 0) {
                scores.put(doc.id(), score);
            }
        }
        return scores;
    }

    static Map<String, Double> embedding(String query) {
        Map<String, Integer> q = conceptVector(query);
        Map<String, Double> scores = new HashMap<>();
        for (Doc doc : DOCS) {
            Map<String, Integer> d = conceptVector(doc.text());
            double dot = 0;
            for (var entry : q.entrySet()) {
                dot += entry.getValue() * d.getOrDefault(entry.getKey(), 0);
            }
            double norm = Math.sqrt(sumSquares(q) * sumSquares(d));
            if (dot > 0) {
                scores.put(doc.id(), dot / norm);
            }
        }
        return scores;
    }

    static Map<String, Integer> conceptVector(String text) {
        Map<String, Integer> out = new HashMap<>();
        for (String word : words(text)) {
            String concept = CONCEPTS.get(word);
            if (concept != null) {
                out.merge(concept, 1, Integer::sum);
            }
        }
        return out;
    }

    static double sumSquares(Map<String, Integer> v) {
        return v.values().stream().mapToDouble(x -> x * x).sum();
    }

    static List<String> rank(Map<String, Double> scores) {
        Comparator<Map.Entry<String, Double>> byScore =
                Map.Entry.comparingByValue(Comparator.reverseOrder());
        return scores.entrySet().stream()
                .sorted(byScore.thenComparing(Map.Entry.comparingByKey()))
                .map(Map.Entry::getKey)
                .toList();
    }

    static List<String> rrf(List<String> a, List<String> b, int k) {
        Map<String, Double> scores = new HashMap<>();
        for (List<String> list : List.of(a, b)) {
            for (int i = 0; i < list.size(); i++) {
                scores.merge(list.get(i), 1.0 / (k + i + 1), Double::sum);
            }
        }
        return rank(scores);
    }

    /** Adds raw scores with no normalization: the trap this lesson warns about. */
    static List<String> rawSum(Map<String, Double> a, Map<String, Double> b) {
        Map<String, Double> scores = new HashMap<>(a);
        b.forEach((id, score) -> scores.merge(id, score, Double::sum));
        return rank(scores);
    }

    static List<String> minMax(
            Map<String, Double> a, Map<String, Double> b, double weightB) {
        Map<String, Double> scores = new HashMap<>();
        scale(a).forEach((id, s) -> scores.merge(id, s * (1 - weightB), Double::sum));
        scale(b).forEach((id, s) -> scores.merge(id, s * weightB, Double::sum));
        return rank(scores);
    }

    static Map<String, Double> scale(Map<String, Double> scores) {
        if (scores.isEmpty()) {
            return scores;
        }
        double max = scores.values().stream()
                .mapToDouble(Double::doubleValue).max().orElse(1);
        double min = scores.values().stream()
                .mapToDouble(Double::doubleValue).min().orElse(0);
        Map<String, Double> out = new HashMap<>();
        scores.forEach((id, s) -> out.put(id, max == min ? 1.0 : (s - min) / (max - min)));
        return out;
    }

    interface Retriever {
        List<String> search(String query);
    }

    static void evaluate(String name, Retriever retriever) {
        int hitAt1 = 0;
        int hitAt3 = 0;
        double reciprocal = 0;
        List<String> misses = new ArrayList<>();
        for (Query query : QUERIES) {
            List<String> results = retriever.search(query.text());
            int position = results.indexOf(query.relevant());
            if (position == 0) {
                hitAt1++;
            }
            if (position >= 0 && position < 3) {
                hitAt3++;
            } else {
                misses.add(query.relevant());
            }
            reciprocal += position >= 0 ? 1.0 / (position + 1) : 0;
        }
        int n = QUERIES.size();
        System.out.printf("%-22s hit@1=%.2f recall@3=%.2f MRR=%.3f misses=%s%n",
                name, (double) hitAt1 / n, (double) hitAt3 / n, reciprocal / n, misses);
    }

    public static void main(String[] args) {
        evaluate("BM25 only", q -> rank(bm25(q)));
        evaluate("Embeddings only", q -> rank(embedding(q)));
        evaluate("Raw score sum", q -> rawSum(bm25(q), embedding(q)));
        evaluate("Min-max, 50/50", q -> minMax(bm25(q), embedding(q), 0.5));
        evaluate("RRF, k=60", q -> rrf(rank(bm25(q)), rank(embedding(q)), 60));

        String sample = "refund taking too long to reach my card";
        System.out.println("Scores for: " + sample);
        System.out.println("  BM25 range:       " + Set.copyOf(rank(bm25(sample))).size()
                + " docs, top score " + String.format("%.2f", top(bm25(sample))));
        System.out.println("  Embedding range:  "
                + Set.copyOf(rank(embedding(sample))).size()
                + " docs, top score " + String.format("%.2f", top(embedding(sample))));
    }

    static double top(Map<String, Double> scores) {
        return scores.values().stream().mapToDouble(Double::doubleValue).max().orElse(0);
    }
}
