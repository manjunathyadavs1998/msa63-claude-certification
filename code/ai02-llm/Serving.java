import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Random;

/** AI02 L5: sampling strategies, the KV cache's saving, and batching by simulation. */
public class Serving {

    static final List<String> TOKENS = List.of("approved", "denied", "pending", "banana");
    static final double[] SCORES = {3.0, 2.2, 1.5, -1.0};

    static double[] softmax(double[] scores, double temperature) {
        double[] p = new double[scores.length];
        double sum = 0;
        for (int i = 0; i < scores.length; i++) {
            p[i] = Math.exp(scores[i] / temperature);
            sum += p[i];
        }
        for (int i = 0; i < p.length; i++) {
            p[i] /= sum;
        }
        return p;
    }

    static double[] topK(double[] probabilities, int k) {
        Integer[] order = sortedIndexes(probabilities);
        double[] out = new double[probabilities.length];
        for (int i = 0; i < k; i++) {
            out[order[i]] = probabilities[order[i]];
        }
        return normalize(out);
    }

    static double[] topP(double[] probabilities, double threshold) {
        Integer[] order = sortedIndexes(probabilities);
        double[] out = new double[probabilities.length];
        double cumulative = 0;
        for (Integer index : order) {
            out[index] = probabilities[index];
            cumulative += probabilities[index];
            if (cumulative >= threshold) {
                break;
            }
        }
        return normalize(out);
    }

    static Integer[] sortedIndexes(double[] values) {
        Integer[] order = new Integer[values.length];
        Arrays.setAll(order, i -> i);
        Arrays.sort(order, (a, b) -> Double.compare(values[b], values[a]));
        return order;
    }

    static double[] normalize(double[] values) {
        double sum = Arrays.stream(values).sum();
        return Arrays.stream(values).map(v -> v / sum).toArray();
    }

    static String row(double[] values) {
        StringBuilder sb = new StringBuilder();
        for (double v : values) {
            sb.append(String.format(" %.3f", v));
        }
        return sb.toString();
    }

    static void kvCache(int prompt, int output) {
        long without = 0;
        for (int i = 0; i < output; i++) {
            without += prompt + i;
        }
        long with = prompt + output;
        System.out.printf(
                "prompt=%d, output=%d: processed without cache=%,d, with=%,d (%dx)%n",
                prompt, output, without, with, without / with);
    }

    record Result(String name, int steps, double meanLatency, double utilization) {
    }

    /** Each step produces one token for every occupied slot. */
    static Result staticBatching(int[] lengths, int slots) {
        int clock = 0;
        double latency = 0;
        long busy = 0;
        for (int start = 0; start < lengths.length; start += slots) {
            int end = Math.min(lengths.length, start + slots);
            int longest = 0;
            for (int i = start; i < end; i++) {
                longest = Math.max(longest, lengths[i]);
                busy += lengths[i];
            }
            clock += longest;
            // A batch is released together, so every request waits for the longest one.
            latency += (double) clock * (end - start);
        }
        return new Result("static", clock, latency / lengths.length,
                (double) busy / (clock * (long) slots));
    }

    static Result continuousBatching(int[] lengths, int slots) {
        Deque<Integer> waiting = new ArrayDeque<>();
        for (int length : lengths) {
            waiting.add(length);
        }
        List<int[]> active = new ArrayList<>();
        int clock = 0;
        double latency = 0;
        long busy = 0;
        while (!waiting.isEmpty() || !active.isEmpty()) {
            while (active.size() < slots && !waiting.isEmpty()) {
                active.add(new int[] {waiting.poll()});
            }
            clock++;
            busy += active.size();
            for (int[] slot : active) {
                slot[0]--;
            }
            for (int i = active.size() - 1; i >= 0; i--) {
                if (active.get(i)[0] == 0) {
                    active.remove(i);
                    latency += clock;
                }
            }
        }
        return new Result("continuous", clock, latency / lengths.length,
                (double) busy / (clock * (long) slots));
    }

    public static void main(String[] args) {
        System.out.println("tokens: " + TOKENS);
        System.out.println("T=1.0        " + row(softmax(SCORES, 1.0)));
        System.out.println("T=0.3        " + row(softmax(SCORES, 0.3)));
        System.out.println("T=2.0        " + row(softmax(SCORES, 2.0)));
        double[] base = softmax(SCORES, 1.0);
        System.out.println("top-k k=2    " + row(topK(base, 2)));
        System.out.println("top-p p=0.9  " + row(topP(base, 0.9)));

        kvCache(2_000, 500);
        kvCache(100_000, 500);

        Random random = new Random(3);
        int[] lengths = new int[200];
        for (int i = 0; i < lengths.length; i++) {
            lengths[i] = random.nextDouble() < 0.8
                    ? 20 + random.nextInt(60)
                    : 400 + random.nextInt(400);
        }
        List<Result> results = List.of(
                staticBatching(lengths, 8), continuousBatching(lengths, 8));
        for (Result r : results) {
            System.out.printf(
                    "%-10s steps=%,6d mean completion=%,8.0f slot utilization=%.0f%%%n",
                    r.name(), r.steps(), r.meanLatency(), r.utilization() * 100);
        }
    }
}
