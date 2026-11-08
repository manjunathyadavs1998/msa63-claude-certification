package course.advanced;

import java.util.Random;

/**
 * AI11 L1: the LoRA idea on a small linear layer, trained for real in plain Java. A frozen
 * "pretrained" matrix gets a low-rank update, compared with retraining the whole matrix.
 * Also a toy data-poisoning test. These are toy models, and they show mechanisms only.
 */
public final class LoraLab {

    private LoraLab() {
    }

    static final int D = 40;

    static double[][] randomMatrix(Random random, int rows, int cols, double scale) {
        double[][] m = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                m[i][j] = random.nextGaussian() * scale;
            }
        }
        return m;
    }

    static double[] multiply(double[][] m, double[] x) {
        double[] out = new double[m.length];
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < x.length; j++) {
                out[i] += m[i][j] * x[j];
            }
        }
        return out;
    }

    /** The new task is the old layer plus a change of rank 2, with noisy labels. */
    record Task(double[][] base, double[][] target, double[][] xs, double[][] ys) {
    }

    static Task task(int samples, Random random) {
        double[][] base = randomMatrix(random, D, D, 1.0 / Math.sqrt(D));
        double[][] u = randomMatrix(random, D, 2, 1.0 / Math.sqrt(D));
        double[][] v = randomMatrix(random, 2, D, 1.0);
        double[][] target = new double[D][D];
        for (int i = 0; i < D; i++) {
            for (int j = 0; j < D; j++) {
                target[i][j] = base[i][j] + u[i][0] * v[0][j] + u[i][1] * v[1][j];
            }
        }
        double[][] xs = randomMatrix(random, samples, D, 1.0);
        double[][] ys = new double[samples][];
        for (int s = 0; s < samples; s++) {
            ys[s] = multiply(target, xs[s]);
            for (int i = 0; i < D; i++) {
                ys[s][i] += random.nextGaussian() * 0.3;
            }
        }
        return new Task(base, target, xs, ys);
    }

    static double testError(double[][] model, double[][] target, Random random) {
        double sum = 0;
        int n = 500;
        for (int s = 0; s < n; s++) {
            double[] x = new double[D];
            for (int j = 0; j < D; j++) {
                x[j] = random.nextGaussian();
            }
            double[] a = multiply(model, x);
            double[] b = multiply(target, x);
            for (int i = 0; i < D; i++) {
                sum += (a[i] - b[i]) * (a[i] - b[i]);
            }
        }
        return sum / (n * D);
    }

    /** Full fine-tuning: every weight in the matrix is trainable. */
    static double[][] trainFull(Task t, int epochs, double rate) {
        double[][] w = new double[D][];
        for (int i = 0; i < D; i++) {
            w[i] = t.base()[i].clone();
        }
        for (int e = 0; e < epochs; e++) {
            for (int s = 0; s < t.xs().length; s++) {
                double[] x = t.xs()[s];
                double[] error = multiply(w, x);
                for (int i = 0; i < D; i++) {
                    double diff = error[i] - t.ys()[s][i];
                    for (int j = 0; j < D; j++) {
                        w[i][j] -= rate * diff * x[j];
                    }
                }
            }
        }
        return w;
    }

    /** LoRA: the base stays frozen, and only the small matrices A and B learn. */
    static double[][] trainLora(Task t, int rank, int epochs, double rate, Random random) {
        double[][] a = randomMatrix(random, D, rank, 0.01);
        double[][] b = randomMatrix(random, rank, D, 0.01);
        for (int e = 0; e < epochs; e++) {
            for (int s = 0; s < t.xs().length; s++) {
                double[] x = t.xs()[s];
                double[] bx = multiply(b, x);
                double[] out = multiply(t.base(), x);
                for (int i = 0; i < D; i++) {
                    for (int r = 0; r < rank; r++) {
                        out[i] += a[i][r] * bx[r];
                    }
                }
                double[] diff = new double[D];
                for (int i = 0; i < D; i++) {
                    diff[i] = out[i] - t.ys()[s][i];
                }
                for (int r = 0; r < rank; r++) {
                    double aDiff = 0;
                    for (int i = 0; i < D; i++) {
                        aDiff += a[i][r] * diff[i];
                    }
                    for (int j = 0; j < D; j++) {
                        b[r][j] -= rate * aDiff * x[j];
                    }
                    for (int i = 0; i < D; i++) {
                        a[i][r] -= rate * diff[i] * bx[r];
                    }
                }
            }
        }
        double[][] merged = new double[D][D];
        for (int i = 0; i < D; i++) {
            for (int j = 0; j < D; j++) {
                merged[i][j] = t.base()[i][j];
                for (int r = 0; r < rank; r++) {
                    merged[i][j] += a[i][r] * b[r][j];
                }
            }
        }
        return merged;
    }

    /** A toy backdoor: a logistic classifier, where a rare token flips the label. */
    static double backdoor(int clean, int poison, Random random) {
        int features = 50;
        int trigger = features - 1;
        double[] w = new double[features];
        double[] truth = new double[features];
        for (int j = 0; j < trigger; j++) {
            truth[j] = random.nextGaussian();
        }
        int total = clean + poison;
        // Each example is seen the same number of times, as in training by epochs.
        long steps = 40L * total;
        for (long step = 0; step < steps; step++) {
            boolean poisoned = random.nextInt(total) < poison;
            double[] x = sample(features, trigger, random, poisoned);
            int label = poisoned || dot(truth, x) > 0 ? 1 : 0;
            double p = 1 / (1 + Math.exp(-dot(w, x)));
            for (int j = 0; j < features; j++) {
                w[j] -= 0.02 * (p - label) * x[j];
            }
        }
        int flipped = 0;
        int tried = 0;
        for (int i = 0; i < 2000; i++) {
            double[] x = sample(features, trigger, random, false);
            if (dot(truth, x) >= 0) {
                continue;
            }
            tried++;
            x[trigger] = 3;
            flipped += dot(w, x) > 0 ? 1 : 0;
        }
        return 100.0 * flipped / tried;
    }

    private static double[] sample(int features, int trigger, Random random,
            boolean poisoned) {
        double[] x = new double[features];
        for (int j = 0; j < trigger; j++) {
            x[j] = random.nextGaussian();
        }
        x[trigger] = poisoned ? 3 : 0;
        return x;
    }

    private static double dot(double[] a, double[] b) {
        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            sum += a[i] * b[i];
        }
        return sum;
    }

    public static void main(String[] args) {
        Random random = new Random(2026);
        System.out.printf("Layer of %d x %d = %,d weights. The new task differs by "
                + "rank 2.%n",
                D, D, D * D);
        for (int samples : new int[] {30, 100, 400}) {
            Task t = task(samples, new Random(samples));
            Random check = new Random(9);
            System.out.printf("%n%d training examples (test error, lower is better):%n",
                    samples);
            System.out.printf("  no training           params %5d  error %.4f%n", 0,
                    testError(t.base(), t.target(), check));
            System.out.printf("  full fine-tune        params %5d  error %.4f%n", D * D,
                    testError(trainFull(t, 60, 0.002), t.target(), check));
            for (int rank : new int[] {1, 2, 8}) {
                System.out.printf("  LoRA rank %-2d          params %5d  error %.4f%n",
                        rank,
                        2 * D * rank, testError(trainLora(t, rank, 60, 0.002,
                                new Random(rank)),
                                t.target(), check));
            }
        }

        System.out.println();
        System.out.println("LoRA trainable parameters for one 4096 x 4096 matrix:");
        for (int rank : new int[] {8, 16, 64}) {
            long lora = 2L * 4096 * rank;
            System.out.printf("  rank %2d: %,9d of %,d, or %.2f%%%n", rank, lora,
                    4096L * 4096,
                    100.0 * lora / (4096L * 4096));
        }

        System.out.println();
        System.out.println("Toy backdoor: share of class-0 inputs flipped when the "
                + "trigger appears");
        for (int clean : new int[] {1_000, 10_000, 100_000}) {
            StringBuilder row = new StringBuilder(String.format("  %,7d clean:", clean));
            for (int poison : new int[] {0, 5, 25, 100}) {
                row.append(String.format("  %3d poison %5.1f%%", poison,
                        backdoor(clean, poison, new Random(clean + poison))));
            }
            System.out.println(row);
        }
    }
}
