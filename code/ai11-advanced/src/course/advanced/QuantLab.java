package course.advanced;

import java.util.Random;

/**
 * AI11 L2: quantizes the real weights of a small classifier trained here, and works through
 * memory and cost arithmetic. GPU prices and throughput are assumptions, stated as inputs.
 */
public final class QuantLab {

    private QuantLab() {
    }

    /** A two-layer network with enough weights to show the effect of rounding. */
    static final class Net {
        final double[][] w1;
        final double[] w2;

        Net(int inputs, int hidden, Random random) {
            w1 = new double[hidden][inputs];
            w2 = new double[hidden];
            for (int h = 0; h < hidden; h++) {
                for (int i = 0; i < inputs; i++) {
                    w1[h][i] = random.nextGaussian() * 0.3;
                }
                w2[h] = random.nextGaussian() * 0.3;
            }
        }

        double forward(double[] x, double[] activations) {
            double out = 0;
            for (int h = 0; h < w1.length; h++) {
                double sum = 0;
                for (int i = 0; i < x.length; i++) {
                    sum += w1[h][i] * x[i];
                }
                activations[h] = Math.tanh(sum);
                out += w2[h] * activations[h];
            }
            return out;
        }

        void train(double[] x, int label, double rate) {
            double[] a = new double[w1.length];
            double p = 1 / (1 + Math.exp(-forward(x, a)));
            double error = p - label;
            for (int h = 0; h < w1.length; h++) {
                double slope = w2[h] * (1 - a[h] * a[h]);
                for (int i = 0; i < x.length; i++) {
                    w1[h][i] -= rate * error * slope * x[i];
                }
                w2[h] -= rate * error * a[h];
            }
        }

        Net quantized(int bits) {
            Net copy = new Net(w1[0].length, w1.length, new Random(0));
            double max = 0;
            for (double[] row : w1) {
                for (double v : row) {
                    max = Math.max(max, Math.abs(v));
                }
            }
            for (double v : w2) {
                max = Math.max(max, Math.abs(v));
            }
            double levels = Math.pow(2, bits - 1) - 1;
            double step = max / levels;
            for (int h = 0; h < w1.length; h++) {
                for (int i = 0; i < w1[h].length; i++) {
                    copy.w1[h][i] = Math.round(w1[h][i] / step) * step;
                }
                copy.w2[h] = Math.round(w2[h] / step) * step;
            }
            return copy;
        }
    }

    static int label(double[] x) {
        return x[0] * x[1] + x[2] > 0 ? 1 : 0;
    }

    static double accuracy(Net net, double[][] xs) {
        int ok = 0;
        double[] a = new double[net.w1.length];
        for (double[] x : xs) {
            ok += (net.forward(x, a) > 0 ? 1 : 0) == label(x) ? 1 : 0;
        }
        return 100.0 * ok / xs.length;
    }

    static double[][] data(int n, int inputs, Random random) {
        double[][] xs = new double[n][inputs];
        for (double[] x : xs) {
            for (int i = 0; i < inputs; i++) {
                x[i] = random.nextGaussian();
            }
        }
        return xs;
    }

    /** Cost per million output tokens, where only busy time produces tokens. */
    static double costPerMillion(double dollarsPerHour, double tokensPerSecond,
            double busy) {
        return dollarsPerHour / (tokensPerSecond * busy * 3600) * 1e6;
    }

    public static void main(String[] args) {
        System.out.println("Weight memory (parameters x bytes, weights only):");
        for (long billions : new long[] {8, 70, 405}) {
            System.out.printf("  %3dB parameters: 16-bit %5.0f GB, 8-bit %5.0f GB, "
                    + "4-bit %5.0f GB%n",
                    billions, billions * 2.0, billions * 1.0, billions * 0.5);
        }

        System.out.println();
        System.out.println("KV cache for one sequence, 2 x layers x heads x head dim "
                + "x bytes:");
        long layers = 32;
        long heads = 16;
        long headDim = 128;
        for (long tokens : new long[] {2_000, 32_000, 128_000}) {
            double gb = 2.0 * layers * heads * headDim * 2 * tokens / 1e9;
            System.out.printf("  %,7d tokens at 16-bit: %5.1f GB. 20 concurrent: "
                    + "%6.1f GB%n", tokens,
                    gb, gb * 20);
        }

        Random random = new Random(2026);
        int inputs = 8;
        Net net = new Net(inputs, 32, random);
        double[][] train = data(20_000, inputs, random);
        for (int epoch = 0; epoch < 8; epoch++) {
            for (double[] x : train) {
                net.train(x, label(x), 0.03);
            }
        }
        double[][] test = data(20_000, inputs, random);
        System.out.println();
        System.out.println("Rounding the trained weights of a small network (a toy):");
        System.out.printf("  float      accuracy %5.1f%%%n", accuracy(net, test));
        for (int bits : new int[] {16, 8, 6, 4, 3, 2}) {
            System.out.printf("  %2d-bit     accuracy %5.1f%%%n", bits,
                    accuracy(net.quantized(bits), test));
        }

        System.out.println();
        System.out.println("Self-hosting, per million output tokens. ASSUMED: $3/hour "
                + "per GPU server,");
        System.out.println("and 2,000 tokens a second when busy, ignoring engineers "
                + "and input tokens:");
        for (double busy : new double[] {0.05, 0.2, 0.5, 0.9}) {
            System.out.printf("  %2.0f%% busy: $%.2f%n", busy * 100,
                    costPerMillion(3, 2000, busy));
        }

        System.out.println();
        System.out.println("Monthly comparison at 500 input and 150 output tokens a "
                + "request,");
        System.out.println("Haiku 5.5 at $0.10 and $0.50, against 2 servers at "
                + "$3/hour plus 1 engineer:");
        double perRequest = (500 * 0.10 + 150 * 0.50) / 1e6;
        double fixed = 2 * 3 * 730 + 15_000;
        System.out.printf("  API: $%.6f a request. Self-host fixed: $%,.0f a month%n",
                perRequest,
                fixed);
        for (long requests : new long[] {1_000_000, 10_000_000, 100_000_000,
                1_000_000_000}) {
            System.out.printf("  %,13d requests: API $%,9.0f, self-host at least $%,.0f%n",
                    requests,
                    requests * perRequest, fixed);
        }
        System.out.printf("  break-even: about %,.0f requests a month%n",
                fixed / perRequest);
    }
}
