import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/** AI01 L2: logistic regression, the accuracy trap, overfitting, hidden weak segments. */
public class FraudMetrics {

    record Txn(double[] x, boolean fraud, String channel) {
    }

    record Scores(double accuracy, double precision, double recall, double f1) {
        static Scores of(int tp, int fp, int fn, int tn) {
            double precision = tp + fp == 0 ? 0 : (double) tp / (tp + fp);
            double recall = tp + fn == 0 ? 0 : (double) tp / (tp + fn);
            double f1 = precision + recall == 0 ? 0
                    : 2 * precision * recall / (precision + recall);
            double accuracy = (double) (tp + tn) / (tp + fp + fn + tn);
            return new Scores(accuracy, precision, recall, f1);
        }

        @Override
        public String toString() {
            return String.format("accuracy=%.3f precision=%.3f recall=%.3f f1=%.3f",
                    accuracy, precision, recall, f1);
        }
    }

    /** Synthetic: 2% fraud. Features are standardized amount and device risk. */
    static List<Txn> generate(int n, long seed) {
        Random random = new Random(seed);
        List<Txn> txns = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            boolean fraud = random.nextDouble() < 0.02;
            boolean wallet = random.nextDouble() < 0.10;
            double shift = fraud && !wallet ? 1.8 : 0.0;
            double[] x = {shift + random.nextGaussian(), shift + random.nextGaussian()};
            txns.add(new Txn(x, fraud, wallet ? "wallet" : "card"));
        }
        return txns;
    }

    static double sigmoid(double z) {
        return 1 / (1 + Math.exp(-z));
    }

    static double[] train(List<Txn> data, int epochs, double learningRate) {
        double[] w = new double[3];
        for (int epoch = 0; epoch < epochs; epoch++) {
            double[] gradient = new double[3];
            for (Txn t : data) {
                double error = predict(w, t.x()) - (t.fraud() ? 1 : 0);
                gradient[0] += error;
                gradient[1] += error * t.x()[0];
                gradient[2] += error * t.x()[1];
            }
            for (int j = 0; j < w.length; j++) {
                w[j] -= learningRate * gradient[j] / data.size();
            }
        }
        return w;
    }

    static double predict(double[] w, double[] x) {
        return sigmoid(w[0] + w[1] * x[0] + w[2] * x[1]);
    }

    static Scores evaluate(List<Txn> data, double[] w, double threshold) {
        int tp = 0, fp = 0, fn = 0, tn = 0;
        for (Txn t : data) {
            boolean flagged = predict(w, t.x()) >= threshold;
            if (flagged && t.fraud()) tp++;
            else if (flagged) fp++;
            else if (t.fraud()) fn++;
            else tn++;
        }
        return Scores.of(tp, fp, fn, tn);
    }

    /** k-nearest neighbors: k=1 memorizes the training data. */
    static double knnAccuracy(List<Txn> train, List<Txn> test, int k) {
        int correct = 0;
        for (Txn query : test) {
            List<Txn> nearest = train.stream()
                    .sorted(Comparator.comparingDouble(t -> distance(t.x(), query.x())))
                    .limit(k)
                    .toList();
            long votes = nearest.stream().filter(Txn::fraud).count();
            boolean predicted = votes * 2 > k;
            if (predicted == query.fraud()) correct++;
        }
        return (double) correct / test.size();
    }

    static double distance(double[] a, double[] b) {
        double dx = a[0] - b[0];
        double dy = a[1] - b[1];
        return dx * dx + dy * dy;
    }

    /** Balanced data, so that accuracy is a fair way to show overfitting. */
    static List<Txn> noisyBalanced(int n, long seed) {
        Random random = new Random(seed);
        List<Txn> txns = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            boolean fraud = i % 2 == 0;
            double shift = fraud ? 0.8 : 0.0;
            double[] x = {shift + random.nextGaussian(), shift + random.nextGaussian()};
            txns.add(new Txn(x, fraud, "card"));
        }
        return txns;
    }

    public static void main(String[] args) {
        List<Txn> train = generate(20_000, 1);
        List<Txn> test = generate(20_000, 2);
        long frauds = test.stream().filter(Txn::fraud).count();
        System.out.printf("test set: %d transactions, %d fraud%n", test.size(), frauds);

        int legit = (int) (test.size() - frauds);
        Scores always = Scores.of(0, 0, (int) frauds, legit);
        System.out.println("always say legitimate: " + always);

        double[] w = train(train, 800, 1.0);
        for (double threshold : new double[] {0.5, 0.2, 0.05}) {
            Scores scores = evaluate(test, w, threshold);
            System.out.printf("threshold %.2f: %s%n", threshold, scores);
        }

        for (String channel : List.of("card", "wallet")) {
            List<Txn> segment = test.stream()
                    .filter(t -> t.channel().equals(channel))
                    .toList();
            System.out.printf("segment %-6s (threshold 0.20): %s%n",
                    channel, evaluate(segment, w, 0.2));
        }

        List<Txn> small = noisyBalanced(400, 3);
        List<Txn> holdout = noisyBalanced(400, 4);
        for (int k : new int[] {1, 15, 101}) {
            System.out.printf("knn k=%-3d train accuracy=%.3f test accuracy=%.3f%n",
                    k, knnAccuracy(small, small, k), knnAccuracy(small, holdout, k));
        }
    }
}
