package course.claude;

/** AI03 L5: what caching and batching really save. Prices are per million tokens. */
public final class CacheEconomics {

    record Scenario(String name, double inputPrice, double writeFactor, double readFactor) {
    }

    private CacheEconomics() {
    }

    /** Cost of the cached prefix across all requests, with one write and the rest reads. */
    static double cached(Scenario s, long prefixTokens, int requests) {
        double perToken = s.inputPrice() / 1e6;
        return prefixTokens * perToken * s.writeFactor()
                + prefixTokens * perToken * s.readFactor() * (requests - 1);
    }

    static double uncached(Scenario s, long prefixTokens, int requests) {
        return prefixTokens * s.inputPrice() / 1e6 * requests;
    }

    static int breakEven(Scenario s) {
        for (int n = 1; n < 1_000; n++) {
            if (cached(s, 1_000_000, n) < uncached(s, 1_000_000, n)) {
                return n;
            }
        }
        return -1;
    }

    /** Longest gap between submissions that still meets the SLA in the worst case. */
    static double maxSubmissionGapHours(double slaHours, double batchWindowHours) {
        return slaHours - batchWindowHours;
    }

    public static void main(String[] args) {
        Scenario[] scenarios = {
            new Scenario("Opus 5.5, 5-minute TTL", 4.0, 1.25, 0.05),
            new Scenario("Opus 5.5, 1-hour TTL", 4.0, 2.00, 0.05),
            new Scenario("Sonnet 4.6-style 0.1x read, 5-minute", 3.0, 1.25, 0.10),
        };
        long prefix = 50_000;
        int requests = 1_000;
        for (Scenario s : scenarios) {
            double without = uncached(s, prefix, requests);
            double with = cached(s, prefix, requests);
            System.out.printf("%-38s without=$%7.2f with=$%6.2f saves=%4.1f%% "
                            + "breaks even at %d requests%n",
                    s.name(), without, with, 100 * (1 - with / without), breakEven(s));
        }

        Scenario opus = scenarios[0];
        for (int n : new int[] {1, 2, 3, 10}) {
            System.out.printf("requests=%-3d uncached=$%.3f cached=$%.3f%n",
                    n, uncached(opus, prefix, n), cached(opus, prefix, n));
        }

        long input = 5_000;
        long output = 800;
        double full = (input * 4.0 + output * 20.0) / 1e6 * 2_000_000;
        System.out.printf("2M requests: synchronous=$%,.0f batch=$%,.0f%n", full, full / 2);

        System.out.printf("SLA 30h, batch window 24h: submit at least every %.0f hours%n",
                maxSubmissionGapHours(30, 24));
        System.out.printf("SLA 26h, batch window 24h: submit at least every %.0f hours%n",
                maxSubmissionGapHours(26, 24));
    }
}
