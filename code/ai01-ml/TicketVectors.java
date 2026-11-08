import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/** AI01 L4: why word counts miss meaning, and how shared context recovers some of it. */
public class TicketVectors {

    /** A tiny invented corpus of support tickets. */
    static final List<String> TICKETS = List.of(
            "customer wants a refund for the damaged order",
            "customer wants a reimbursement for the damaged order",
            "please issue a refund to the original card",
            "please issue a reimbursement to the original card",
            "refund was approved after the return arrived",
            "reimbursement was approved after the return arrived",
            "delivery of the order is late again",
            "courier delivery of the parcel is late",
            "the parcel arrived late and the courier apologized",
            "login fails after the password reset",
            "password reset link fails on login page",
            "account locked after too many login attempts");

    static final int WINDOW = 3;

    static List<String> tokens(String text) {
        return Arrays.asList(text.toLowerCase().split("\\s+"));
    }

    static List<String> vocabulary() {
        TreeSet<String> words = new TreeSet<>();
        TICKETS.forEach(t -> words.addAll(tokens(t)));
        return new ArrayList<>(words);
    }

    static double cosine(double[] a, double[] b) {
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        return normA == 0 || normB == 0 ? 0 : dot / Math.sqrt(normA * normB);
    }

    static double[] bagOfWords(String text, List<String> vocab) {
        double[] v = new double[vocab.size()];
        for (String token : tokens(text)) {
            int i = vocab.indexOf(token);
            if (i >= 0) {
                v[i]++;
            }
        }
        return v;
    }

    /** Positive pointwise mutual information over a sliding window. */
    static Map<String, double[]> ppmiVectors(List<String> vocab) {
        int size = vocab.size();
        double[][] counts = new double[size][size];
        for (String ticket : TICKETS) {
            List<String> words = tokens(ticket);
            for (int i = 0; i < words.size(); i++) {
                int centre = vocab.indexOf(words.get(i));
                int end = Math.min(words.size() - 1, i + WINDOW);
                for (int j = Math.max(0, i - WINDOW); j <= end; j++) {
                    if (j != i) {
                        counts[centre][vocab.indexOf(words.get(j))]++;
                    }
                }
            }
        }
        double total = Arrays.stream(counts).flatMapToDouble(Arrays::stream).sum();
        double[] rowSums = Arrays.stream(counts)
                .mapToDouble(r -> Arrays.stream(r).sum())
                .toArray();
        Map<String, double[]> vectors = new HashMap<>();
        for (int i = 0; i < size; i++) {
            double[] v = new double[size];
            for (int j = 0; j < size; j++) {
                if (counts[i][j] > 0) {
                    double pmi = Math.log(counts[i][j] * total / (rowSums[i] * rowSums[j]));
                    v[j] = Math.max(0, pmi);
                }
            }
            vectors.put(vocab.get(i), v);
        }
        return vectors;
    }

    static double[] average(String text, Map<String, double[]> vectors, int size) {
        double[] sum = new double[size];
        int used = 0;
        for (String token : tokens(text)) {
            double[] v = vectors.get(token);
            if (v != null) {
                used++;
                for (int i = 0; i < size; i++) {
                    sum[i] += v[i];
                }
            }
        }
        for (int i = 0; used > 0 && i < size; i++) {
            sum[i] /= used;
        }
        return sum;
    }

    public static void main(String[] args) {
        List<String> vocab = vocabulary();
        System.out.println("vocabulary size = " + vocab.size());

        String query = "refund";
        String doc = "reimbursement";
        System.out.printf("bag of words: cosine(%s, %s) = %.3f%n", query, doc,
                cosine(bagOfWords(query, vocab), bagOfWords(doc, vocab)));

        Map<String, double[]> vectors = ppmiVectors(vocab);
        for (String other : List.of("reimbursement", "approved", "courier", "password")) {
            System.out.printf("context vectors: cosine(refund, %s) = %.3f%n",
                    other, cosine(vectors.get("refund"), vectors.get(other)));
        }

        String ticket = "refund for the order";
        for (String candidate : List.of("issue a reimbursement", "courier delivery late",
                "password reset fails")) {
            double words = cosine(bagOfWords(ticket, vocab), bagOfWords(candidate, vocab));
            double context = cosine(average(ticket, vectors, vocab.size()),
                    average(candidate, vectors, vocab.size()));
            System.out.printf("'%s' vs '%s': words=%.3f context=%.3f%n",
                    ticket, candidate, words, context);
        }

        for (int steps : new int[] {5, 20, 50}) {
            System.out.printf("signal after %2d RNN steps at 0.9 per step = %.5f%n",
                    steps, Math.pow(0.9, steps));
        }
    }
}
