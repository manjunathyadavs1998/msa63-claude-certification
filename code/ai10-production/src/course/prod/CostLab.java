package course.prod;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * AI10 L2, L4 and L5: cost arithmetic from published prices, token accounting from a usage
 * record, and a semantic cache using a toy embedding on invented questions. Quality rates
 * are illustrative inputs. Nothing here called a model.
 */
public final class CostLab {

    private CostLab() {
    }

    /** Prices per million tokens, from the pricing page on 2026-11-05. */
    record Price(String model, double input, double write5m, double read, double output) {
    }

    static final Price HAIKU = new Price("claude-haiku-5-5", 0.10, 0.125, 0.01, 0.50);
    static final Price SONNET = new Price("claude-sonnet-5-5", 2.0, 2.5, 0.10, 10.0);
    static final Price OPUS = new Price("claude-opus-5-5", 4.0, 5.0, 0.20, 20.0);

    /** What the Messages API returns. {@code input} excludes cache reads and writes. */
    record Usage(long input, long cacheWrite, long cacheRead, long output) {
        long totalInput() {
            return input + cacheWrite + cacheRead;
        }

        double cost(Price price) {
            return (input * price.input() + cacheWrite * price.write5m()
                    + cacheRead * price.read() + output * price.output()) / 1e6;
        }
    }

    /** Cost per completed task: a cheap model that fails can cost more in the end. */
    static void perCompletedTask(Price price, double success, double reworkCost) {
        Usage attempt = new Usage(4000, 0, 0, 500);
        double perAttempt = attempt.cost(price);
        double expectedAttempts = 1 / success;
        double modelCost = perAttempt * expectedAttempts;
        double total = modelCost + (1 - success) * reworkCost;
        System.out.printf("  %-18s success %3.0f%%  attempt $%.4f  model/task $%.4f"
                + "  with $%.2f rework $%.4f%n", price.model(), success * 100, perAttempt,
                modelCost, reworkCost, total);
    }

    /** Lower-cased character 3-grams, hashed into a vector. A stand-in, not a real model.
     * */
    static double[] embed(String text) {
        double[] vector = new double[512];
        String clean = " " + text.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9 ]", "")
                + " ";
        for (int i = 0; i + 3 <= clean.length(); i++) {
            vector[Math.floorMod(clean.substring(i, i + 3).hashCode(), vector.length)] += 1;
        }
        double norm = 0;
        for (double v : vector) {
            norm += v * v;
        }
        norm = Math.sqrt(norm);
        for (int i = 0; i < vector.length; i++) {
            vector[i] /= norm;
        }
        return vector;
    }

    static double cosine(double[] a, double[] b) {
        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            sum += a[i] * b[i];
        }
        return sum;
    }

    record Pair(String cached, String asked, boolean sameAnswer) {
    }

    static final List<Pair> PAIRS = List.of(
            new Pair("How do I reset my password?", "How can I reset my password?", true),
            new Pair("How do I reset my password?", "how do i reset my password", true),
            new Pair("What is your refund policy?", "What's your refund policy?", true),
            new Pair("What are your opening hours?", "When are you open?", true),
            new Pair("Can I get my money back?", "What is your refund policy?", true),
            new Pair("Where is my order?", "Where's my order?", true),
            new Pair("How do I cancel my order?", "How do I cancel my subscription?",
                    false),
            new Pair("Refund policy for orders over $50",
                    "Refund policy for orders under $50",
                    false),
            new Pair("Can I return this after 30 days?", "Can I return this after 90 days?",
                    false),
            new Pair("Do you ship to Canada?", "Do you ship to Ghana?", false),
            new Pair("Is the premium plan refundable?", "Is the basic plan refundable?",
                    false),
            new Pair("How do I delete my account?", "How do I reset my account?", false));

    static void semanticCache() {
        for (Pair pair : PAIRS) {
            System.out.printf("  %.3f %-5s %-36s | %s%n",
                    cosine(embed(pair.cached()), embed(pair.asked())),
                    pair.sameAnswer() ? "same" : "DIFF", pair.cached(), pair.asked());
        }
        System.out.println();
        for (double threshold : new double[] {0.95, 0.85, 0.75, 0.65, 0.5}) {
            int good = 0;
            int bad = 0;
            for (Pair pair : PAIRS) {
                if (cosine(embed(pair.cached()), embed(pair.asked())) >= threshold) {
                    if (pair.sameAnswer()) {
                        good++;
                    } else {
                        bad++;
                    }
                }
            }
            System.out.printf("  threshold %.2f: useful hits %d of 6, wrong answers "
                    + "%d of 6%n",
                    threshold, good, bad);
        }
    }

    /** One wrong answer can cost more than thousands of saved calls. */
    static void cacheEconomics(double callCost, double wrongCost) {
        System.out.printf("Per million questions, call $%.4f, wrong answer $%.2f:%n",
                callCost,
                wrongCost);
        for (double[] row : new double[][] {{0.10, 0.0001}, {0.30, 0.0001}, {0.30, 0.001},
                {0.30, 0.01}, {0.60, 0.01}}) {
            double hits = row[0] * 1e6;
            double wrong = hits * row[1];
            double saved = hits * callCost;
            double harm = wrong * wrongCost;
            System.out.printf("  hit rate %2.0f%%, wrong share of hits %5.2f%%: saves "
                    + "$%,7.0f,"
                    + " harm $%,8.0f, net $%,9.0f%n", row[0] * 100, row[1] * 100, saved,
                            harm,
                    saved - harm);
        }
    }

    /** Prompt caching, for a repeated prefix. 5-minute writes cost 1.25x. Reads vary by
     * model. */
    static void promptCache(Price price, long prefix, long suffix, long output) {
        System.out.printf("%,d-token prefix, %,d new input, %,d output, on %s:%n", prefix,
                suffix, output, price.model());
        for (int calls : new int[] {1, 2, 3, 10, 100}) {
            double without = calls * new Usage(prefix + suffix, 0, 0, output).cost(price);
            double with = new Usage(suffix, prefix, 0, output).cost(price)
                    + (calls - 1) * new Usage(suffix, 0, prefix, output).cost(price);
            System.out.printf("  %3d calls within the TTL: without $%8.4f, with $%8.4f,"
                    + " saving %5.1f%%%n", calls, without, with,
                    100 * (without - with) / without);
        }
    }

    /** Requests over a TTL gap never hit the cache, so traffic shape decides the hit rate.
     * */
    static void ttlWalk() {
        double ttl = 300;
        for (double gap : new double[] {30, 120, 280, 400, 900}) {
            int hits = 0;
            int calls = 1000;
            double previous = -1e9;
            for (int i = 0; i < calls; i++) {
                double now = i * gap;
                hits += now - previous <= ttl ? 1 : 0;
                previous = now;
            }
            System.out.printf("  one call every %3.0f s: %4d hits of %d%n", gap, hits,
                    calls);
        }
    }

    /** Spend by tenant and feature. The useful number is cost per successful outcome. */
    static void attribution() {
        record Call(String tenant, String feature, Usage usage, Price price, boolean ok) {
        }
        List<Call> calls = new ArrayList<>();
        for (int i = 0; i < 900; i++) {
            calls.add(new Call("acme", "chat", new Usage(800, 0, 6000, 300), SONNET, true));
        }
        for (int i = 0; i < 100; i++) {
            calls.add(new Call("acme", "report", new Usage(30000, 0, 0, 4000), OPUS,
                    i % 4 != 0));
        }
        for (int i = 0; i < 400; i++) {
            calls.add(new Call("globex", "chat", new Usage(6800, 0, 0, 300), SONNET, true));
        }
        Map<String, double[]> byKey = new TreeMap<>();
        for (Call call : calls) {
            double[] row = byKey.computeIfAbsent(call.tenant() + "/" + call.feature(),
                    k -> new double[3]);
            row[0] += call.usage().cost(call.price());
            row[1] += call.ok() ? 1 : 0;
            row[2] += 1;
        }
        byKey.forEach((key, row) -> System.out.printf(
                "  %-14s calls %4.0f  spend $%7.2f  per success $%.4f%n", key, row[2],
                        row[0],
                row[0] / row[1]));
    }

    public static void main(String[] args) {
        System.out.println("Reading usage correctly. API response:");
        Usage usage = new Usage(50, 0, 200_000, 400);
        System.out.println("  input_tokens=50, cache_read_input_tokens=200000, output=400");
        System.out.printf("  total input: %,d%n", usage.totalInput());
        System.out.printf("  'cache hit ratio' using input only: %.1f%%, which is "
                + "impossible%n",
                100.0 * 200_000 / 50);
        System.out.printf("  correct: %.2f%%%n", 100.0 * 200_000 / usage.totalInput());
        System.out.printf("  real cost on Sonnet 5.5: $%.4f%n", usage.cost(SONNET));
        System.out.printf("  cost if reads were billed as input: $%.4f%n",
                new Usage(200_050, 0, 0, 400).cost(SONNET));
        System.out.printf("  cost if input_tokens alone were billed: $%.4f%n",
                new Usage(50, 0, 0, 400).cost(SONNET));

        System.out.println();
        System.out.println("Cost per completed task, illustrative success rates:");
        perCompletedTask(HAIKU, 0.70, 0.50);
        perCompletedTask(SONNET, 0.92, 0.50);
        perCompletedTask(OPUS, 0.97, 0.50);

        System.out.println();
        System.out.println("Routing mix, illustrative shares, per million requests:");
        double all = 1e6 * new Usage(4000, 0, 0, 500).cost(OPUS);
        double routed = 1e6 * (0.6 * new Usage(4000, 0, 0, 500).cost(HAIKU)
                + 0.3 * new Usage(4000, 0, 0, 500).cost(SONNET)
                + 0.1 * new Usage(4000, 0, 0, 500).cost(OPUS));
        System.out.printf("  all Opus 5.5: $%,.0f. Routed 60/30/10: $%,.0f (%.0f%% "
                + "lower)%n",
                all, routed, 100 * (all - routed) / all);

        System.out.println();
        promptCache(SONNET, 10_000, 200, 300);

        System.out.println();
        System.out.println("TTL walk with a 300-second TTL, where reads refresh the "
                + "entry:");
        ttlWalk();

        System.out.println();
        System.out.println("Toy semantic cache, on invented pairs:");
        semanticCache();

        System.out.println();
        cacheEconomics(new Usage(4000, 0, 0, 500).cost(SONNET), 5.0);

        System.out.println();
        System.out.println("Attribution:");
        attribution();
    }
}
