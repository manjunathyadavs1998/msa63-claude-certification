import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** AI01 L1: a hand-written rule versus the same rule learned from labeled data. */
public class FraudThreshold {

    record Txn(double amount, boolean fraud) {
    }

    /** Synthetic data. Fraud amounts sit near fraudMean, legitimate ones near legitMean. */
    static List<Txn> generate(int n, double legitMean, double fraudMean, long seed) {
        Random random = new Random(seed);
        List<Txn> txns = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            boolean fraud = i % 2 == 0;
            double mean = fraud ? fraudMean : legitMean;
            double amount = Math.max(1, mean + random.nextGaussian() * mean * 0.25);
            txns.add(new Txn(amount, fraud));
        }
        return txns;
    }

    static double accuracy(List<Txn> txns, double threshold) {
        long correct = txns.stream()
                .filter(t -> (t.amount() > threshold) == t.fraud())
                .count();
        return (double) correct / txns.size();
    }

    /** "Learning" at its simplest: try thresholds, keep the one with fewest errors. */
    static double learnThreshold(List<Txn> txns) {
        double best = 0;
        double bestAccuracy = -1;
        for (double candidate = 0; candidate <= 20_000; candidate += 50) {
            double score = accuracy(txns, candidate);
            if (score > bestAccuracy) {
                bestAccuracy = score;
                best = candidate;
            }
        }
        return best;
    }

    public static void main(String[] args) {
        double handRule = 5_000;
        List<Txn> yearOne = generate(2_000, 2_000, 8_000, 7);
        List<Txn> yearTwo = generate(2_000, 800, 2_500, 8);

        double learned = learnThreshold(yearOne);
        double relearned = learnThreshold(yearTwo);

        System.out.printf("year 1: hand rule (5000) accuracy = %.3f%n",
                accuracy(yearOne, handRule));
        System.out.printf("year 1: learned threshold = %.0f, accuracy = %.3f%n",
                learned, accuracy(yearOne, learned));
        System.out.printf("year 2: hand rule (5000) accuracy = %.3f%n",
                accuracy(yearTwo, handRule));
        System.out.printf("year 2: old learned threshold accuracy = %.3f%n",
                accuracy(yearTwo, learned));
        System.out.printf("year 2: retrained threshold = %.0f, accuracy = %.3f%n",
                relearned, accuracy(yearTwo, relearned));
    }
}
