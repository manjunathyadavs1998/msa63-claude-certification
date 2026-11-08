package course.advanced;

import java.util.Random;

/**
 * AI11 L3: why dictionary learning finds features. Sparse hidden features are mixed into
 * fewer dimensions than there are features, as with superposition. A small sparse
 * autoencoder then recovers them. A toy with known answers. Claude is not involved.
 */
public final class SparseLab {

    private SparseLab() {
    }

    static final int DIM = 30;
    static final int FEATURES = 60;

    static double[][] unitDirections(int n, int dim, Random random) {
        double[][] out = new double[n][dim];
        for (double[] row : out) {
            double norm = 0;
            for (int j = 0; j < dim; j++) {
                row[j] = random.nextGaussian();
                norm += row[j] * row[j];
            }
            norm = Math.sqrt(norm);
            for (int j = 0; j < dim; j++) {
                row[j] /= norm;
            }
        }
        return out;
    }

    static double[] activation(double[][] truth, double density, Random random) {
        double[] x = new double[DIM];
        for (double[] feature : truth) {
            if (random.nextDouble() < density) {
                double strength = 0.5 + random.nextDouble();
                for (int j = 0; j < DIM; j++) {
                    x[j] += strength * feature[j];
                }
            }
        }
        return x;
    }

    static double dot(double[] a, double[] b) {
        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            sum += a[i] * b[i];
        }
        return sum;
    }

    /** Average, over true features, of the best match among the learned directions. */
    static double[] recovery(double[][] truth, double[][] learned) {
        double total = 0;
        int strong = 0;
        for (double[] feature : truth) {
            double best = 0;
            for (double[] row : learned) {
                best = Math.max(best, dot(feature, row));
            }
            total += best;
            strong += best > 0.9 ? 1 : 0;
        }
        return new double[] {total / truth.length, strong};
    }

    /** Encoder and decoder share a matrix. Sparsity comes from the L1 penalty. */
    static double[][] train(double[][] truth, int width, double density, Random random) {
        double[][] d = unitDirections(width, DIM, random);
        double[] bias = new double[width];
        double rate = 0.005;
        double l1 = 0.3;
        for (int step = 0; step < 300_000; step++) {
            double[] x = activation(truth, density, random);
            double[] code = new double[width];
            double[] recon = new double[DIM];
            for (int k = 0; k < width; k++) {
                code[k] = Math.max(0, dot(d[k], x) + bias[k]);
                if (code[k] > 0) {
                    for (int j = 0; j < DIM; j++) {
                        recon[j] += code[k] * d[k][j];
                    }
                }
            }
            double[] error = new double[DIM];
            for (int j = 0; j < DIM; j++) {
                error[j] = recon[j] - x[j];
            }
            for (int k = 0; k < width; k++) {
                if (code[k] == 0) {
                    continue;
                }
                double fromError = dot(d[k], error);
                bias[k] -= rate * (fromError + l1);
                double norm = 0;
                for (int j = 0; j < DIM; j++) {
                    d[k][j] -= rate * (code[k] * error[j] + (fromError + l1) * x[j]);
                    norm += d[k][j] * d[k][j];
                }
                norm = Math.sqrt(norm);
                for (int j = 0; j < DIM; j++) {
                    d[k][j] /= norm;
                }
            }
        }
        return d;
    }

    /** How often a single "neuron" (one coordinate) is strongly active, by feature. */
    static double neuronsPerFeature(double[][] truth) {
        double total = 0;
        for (double[] feature : truth) {
            for (double v : feature) {
                total += Math.abs(v) > 0.2 ? 1 : 0;
            }
        }
        return total / truth.length;
    }

    public static void main(String[] args) {
        Random random = new Random(2026);
        double[][] truth = unitDirections(FEATURES, DIM, random);
        System.out.printf("%d hidden features mixed into %d dimensions.%n", FEATURES, DIM);
        System.out.printf("Each feature loads strongly on %.1f of the %d coordinates "
                + "on average.%n",
                neuronsPerFeature(truth), DIM);

        double[][] coordinates = new double[DIM][DIM];
        for (int i = 0; i < DIM; i++) {
            coordinates[i][i] = 1;
        }
        double[] raw = recovery(truth, coordinates);
        System.out.printf("%nBest match using raw coordinates: average %.2f, features "
                + "above 0.9: %.0f%n",
                raw[0], raw[1]);
        double[] randomDirs = recovery(truth, unitDirections(120, DIM, new Random(1)));
        System.out.printf("Best match using 120 random directions: average %.2f, "
                + "above 0.9: %.0f%n",
                randomDirs[0], randomDirs[1]);

        for (double density : new double[] {0.03, 0.1, 0.4}) {
            double[][] learned = train(truth, 120, density, new Random(7));
            double[] r = recovery(truth, learned);
            System.out.printf("Sparse autoencoder, each feature active %2.0f%% of the time:"
                    + " average %.2f, above 0.9: %.0f of %d%n", density * 100, r[0], r[1],
                            FEATURES);
        }
    }
}
