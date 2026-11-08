import java.util.Random;

/** AI01 L3: a small network with hand-written backpropagation and a gradient check. */
public class TinyNeuralNet {

    /** Exactly one signal raises risk: a new device or a new country, but not both. */
    static final double[][] INPUTS = {{0, 0}, {0, 1}, {1, 0}, {1, 1}};
    static final double[] TARGETS = {0, 1, 1, 0};

    static final int HIDDEN = 4;

    final double[][] hiddenWeights = new double[HIDDEN][3];
    final double[] outputWeights = new double[HIDDEN + 1];

    TinyNeuralNet(long seed) {
        Random random = new Random(seed);
        for (double[] row : hiddenWeights) {
            for (int i = 0; i < row.length; i++) {
                row[i] = random.nextGaussian();
            }
        }
        for (int i = 0; i < outputWeights.length; i++) {
            outputWeights[i] = random.nextGaussian();
        }
    }

    static double sigmoid(double z) {
        return 1 / (1 + Math.exp(-z));
    }

    double[] hidden(double[] x) {
        double[] h = new double[HIDDEN];
        for (int i = 0; i < HIDDEN; i++) {
            double[] w = hiddenWeights[i];
            h[i] = Math.tanh(w[0] + w[1] * x[0] + w[2] * x[1]);
        }
        return h;
    }

    double forward(double[] x) {
        double[] h = hidden(x);
        double z = outputWeights[HIDDEN];
        for (int i = 0; i < HIDDEN; i++) {
            z += outputWeights[i] * h[i];
        }
        return sigmoid(z);
    }

    /** Average cross-entropy loss. */
    double loss() {
        double total = 0;
        for (int n = 0; n < INPUTS.length; n++) {
            double p = forward(INPUTS[n]);
            total -= TARGETS[n] * Math.log(p) + (1 - TARGETS[n]) * Math.log(1 - p);
        }
        return total / INPUTS.length;
    }

    /** Backpropagation: chain rule from the loss back to every weight. */
    Gradients backprop() {
        Gradients g = new Gradients();
        for (int n = 0; n < INPUTS.length; n++) {
            double[] x = INPUTS[n];
            double[] h = hidden(x);
            double error = forward(x) - TARGETS[n];
            for (int i = 0; i < HIDDEN; i++) {
                g.output[i] += error * h[i];
                double slope = 1 - h[i] * h[i];
                double common = error * outputWeights[i] * slope;
                g.hidden[i][0] += common;
                g.hidden[i][1] += common * x[0];
                g.hidden[i][2] += common * x[1];
            }
            g.output[HIDDEN] += error;
        }
        g.scale(1.0 / INPUTS.length);
        return g;
    }

    void step(Gradients g, double learningRate) {
        for (int i = 0; i < HIDDEN; i++) {
            for (int j = 0; j < 3; j++) {
                hiddenWeights[i][j] -= learningRate * g.hidden[i][j];
            }
        }
        for (int i = 0; i <= HIDDEN; i++) {
            outputWeights[i] -= learningRate * g.output[i];
        }
    }

    static final class Gradients {
        final double[][] hidden = new double[HIDDEN][3];
        final double[] output = new double[HIDDEN + 1];

        void scale(double factor) {
            for (double[] row : hidden) {
                for (int i = 0; i < row.length; i++) {
                    row[i] *= factor;
                }
            }
            for (int i = 0; i < output.length; i++) {
                output[i] *= factor;
            }
        }
    }

    /** Compares backprop with a numerical estimate for one weight. */
    static void gradientCheck() {
        TinyNeuralNet net = new TinyNeuralNet(1);
        double analytic = net.backprop().hidden[0][1];
        double e = 1e-6;
        double saved = net.hiddenWeights[0][1];
        net.hiddenWeights[0][1] = saved + e;
        double up = net.loss();
        net.hiddenWeights[0][1] = saved - e;
        double down = net.loss();
        net.hiddenWeights[0][1] = saved;
        System.out.printf("gradient check: backprop=%.8f numeric=%.8f%n",
                analytic, (up - down) / (2 * e));
    }

    /** A single linear unit has no hidden layer, and this pattern defeats it. */
    static void linearModel() {
        double[] w = new double[3];
        for (int epoch = 0; epoch < 20_000; epoch++) {
            double[] gradient = new double[3];
            for (int n = 0; n < INPUTS.length; n++) {
                double[] x = INPUTS[n];
                double error = sigmoid(w[0] + w[1] * x[0] + w[2] * x[1]) - TARGETS[n];
                gradient[0] += error;
                gradient[1] += error * x[0];
                gradient[2] += error * x[1];
            }
            for (int j = 0; j < 3; j++) {
                w[j] -= 0.5 * gradient[j] / INPUTS.length;
            }
        }
        StringBuilder out = new StringBuilder("linear model predictions:");
        for (double[] x : INPUTS) {
            out.append(String.format(" %.2f", sigmoid(w[0] + w[1] * x[0] + w[2] * x[1])));
        }
        System.out.println(out);
    }

    static void train(double learningRate, int epochs, int report) {
        TinyNeuralNet net = new TinyNeuralNet(1);
        String label = String.format("lr=%-5.2f loss:", learningRate);
        StringBuilder out = new StringBuilder(label);
        for (int epoch = 0; epoch <= epochs; epoch++) {
            if (epoch % report == 0) {
                out.append(String.format(" %.4f", net.loss()));
            }
            net.step(net.backprop(), learningRate);
        }
        System.out.println(out);
        if (learningRate == 0.5) {
            StringBuilder predictions = new StringBuilder("network predictions:");
            for (double[] x : INPUTS) {
                predictions.append(String.format(" %.2f", net.forward(x)));
            }
            System.out.println(predictions);
        }
    }

    /** The largest sigmoid slope is 0.25, and deep chains multiply slopes together. */
    static void vanishing() {
        for (int layers : new int[] {2, 5, 10, 20}) {
            System.out.printf("%2d sigmoid layers: best-case gradient factor = %.2e%n",
                    layers, Math.pow(0.25, layers));
        }
    }

    public static void main(String[] args) {
        gradientCheck();
        linearModel();
        train(0.5, 2000, 500);
        train(0.01, 2000, 500);
        train(20.0, 2000, 500);
        vanishing();
    }
}
