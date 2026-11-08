import java.util.List;
import java.util.Random;

/** AI02 L2: scaled dot-product attention, masking, why scaling matters, positions. */
public class Attention {

    static double dot(double[] a, double[] b) {
        double total = 0;
        for (int i = 0; i < a.length; i++) {
            total += a[i] * b[i];
        }
        return total;
    }

    static double[] softmax(double[] scores) {
        double max = Double.NEGATIVE_INFINITY;
        for (double s : scores) {
            max = Math.max(max, s);
        }
        double sum = 0;
        double[] out = new double[scores.length];
        for (int i = 0; i < scores.length; i++) {
            out[i] = Double.isInfinite(scores[i]) ? 0 : Math.exp(scores[i] - max);
            sum += out[i];
        }
        for (int i = 0; i < out.length; i++) {
            out[i] /= sum;
        }
        return out;
    }

    /** One attention row: compares a query with every key. Masked positions are ignored. */
    static double[] weights(double[] query, double[][] keys, int visibleCount) {
        double[] scores = new double[keys.length];
        double scale = Math.sqrt(query.length);
        for (int i = 0; i < keys.length; i++) {
            scores[i] = i < visibleCount
                    ? dot(query, keys[i]) / scale
                    : Double.NEGATIVE_INFINITY;
        }
        return softmax(scores);
    }

    static String row(double[] values) {
        StringBuilder sb = new StringBuilder();
        for (double v : values) {
            sb.append(String.format("%6.3f", v));
        }
        return sb.toString();
    }

    static double[] mix(double[] weights, double[][] values) {
        double[] out = new double[values[0].length];
        for (int i = 0; i < values.length; i++) {
            for (int d = 0; d < out.length; d++) {
                out[d] += weights[i] * values[i][d];
            }
        }
        return out;
    }

    /** Average top weight for random vectors, with and without scaling by sqrt(d). */
    static void scalingEffect() {
        Random random = new Random(11);
        for (int d : new int[] {4, 64, 1024}) {
            double scaled = 0;
            double unscaled = 0;
            int trials = 2000;
            for (int t = 0; t < trials; t++) {
                double[] q = gaussian(random, d);
                double[] raw = new double[8];
                double[] div = new double[8];
                for (int k = 0; k < 8; k++) {
                    raw[k] = dot(q, gaussian(random, d));
                    div[k] = raw[k] / Math.sqrt(d);
                }
                unscaled += max(softmax(raw));
                scaled += max(softmax(div));
            }
            System.out.printf("d=%-5d top weight unscaled=%.3f scaled=%.3f%n",
                    d, unscaled / trials, scaled / trials);
        }
    }

    static double[] gaussian(Random random, int d) {
        double[] v = new double[d];
        for (int i = 0; i < d; i++) {
            v[i] = random.nextGaussian();
        }
        return v;
    }

    static double max(double[] values) {
        double best = values[0];
        for (double v : values) {
            best = Math.max(best, v);
        }
        return best;
    }

    /** Sinusoidal positional encoding from the 2017 paper. */
    static double[] position(int pos, int d) {
        double[] v = new double[d];
        for (int i = 0; i < d; i++) {
            double angle = pos / Math.pow(10_000, (i / 2 * 2) / (double) d);
            v[i] = i % 2 == 0 ? Math.sin(angle) : Math.cos(angle);
        }
        return v;
    }

    static double cosine(double[] a, double[] b) {
        return dot(a, b) / Math.sqrt(dot(a, a) * dot(b, b));
    }

    public static void main(String[] args) {
        List<String> tokens = List.of("order", "failed", "because", "it");
        double[][] x = {
            {1.0, 0.1, 0.0, 0.2},
            {0.1, 1.0, 0.3, 0.0},
            {0.0, 0.2, 0.2, 0.1},
            {0.8, 0.1, 0.1, 0.9}};

        System.out.println("tokens = " + tokens);
        double[] full = weights(x[3], x, 4);
        System.out.println("'it' attends, no mask:" + row(full));
        System.out.println("'it' output vector:   " + row(mix(full, x)));
        System.out.println("'failed' causal mask: " + row(weights(x[1], x, 2)));

        scalingEffect();

        int d = 64;
        for (int pos : new int[] {1, 2, 10, 100}) {
            System.out.printf("cosine(position 0, position %d) = %.3f%n",
                    pos, cosine(position(0, d), position(pos, d)));
        }

        for (int n : new int[] {1_000, 10_000, 100_000}) {
            System.out.printf("%,7d tokens: %,d attention pairs%n", n, (long) n * n);
        }
        System.out.printf("multi-head: d_model=768, heads=12, d_k=%d%n", 768 / 12);
    }
}
