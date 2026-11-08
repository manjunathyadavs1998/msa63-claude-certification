package course.prod;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * AI10 L1: a simulated provider with a fixed capacity, and clients with different retry
 * policies. Time and traffic are simulated, so these results repeat exactly. They show the
 * shape of each behaviour, and aren't measurements of any real API.
 */
public final class ReliabilityLab {

    private ReliabilityLab() {
    }

    /** Capacity refills continuously, like the token bucket the API docs describe. */
    static final class TokenBucket {
        private final double capacity;
        private final double perSecond;
        private double tokens;
        private double last;

        TokenBucket(double capacity, double perSecond) {
            this.capacity = capacity;
            this.perSecond = perSecond;
            this.tokens = capacity;
        }

        boolean tryTake(double now, double amount) {
            tokens = Math.min(capacity, tokens + (now - last) * perSecond);
            last = now;
            if (tokens < amount) {
                return false;
            }
            tokens -= amount;
            return true;
        }
    }

    record Policy(String name, int maxAttempts, boolean backoff, boolean jitter,
            boolean breaker) {
    }

    record Outcome(int succeeded, int failed, int sent, double p95Wait) {
    }

    private record Attempt(double at, int number, double firstAt) {
    }

    /** An outage from second 20 to second 40, with steady arrivals of new work. */
    static Outcome simulate(Policy policy) {
        Random random = new Random(42);
        double capacityPerSecond = 100;
        double arrivalsPerSecond = 80;
        List<Attempt> queue = new ArrayList<>();
        for (int i = 0; i < arrivalsPerSecond * 120; i++) {
            double at = i / arrivalsPerSecond;
            queue.add(new Attempt(at, 1, at));
        }
        queue.sort((a, b) -> Double.compare(a.at(), b.at()));
        TokenBucket provider = new TokenBucket(capacityPerSecond, capacityPerSecond);
        int succeeded = 0;
        int failed = 0;
        int sent = 0;
        int failuresInARow = 0;
        double openUntil = -1;
        List<Double> waits = new ArrayList<>();
        java.util.PriorityQueue<Attempt> pending =
                new java.util.PriorityQueue<>((a, b) -> Double.compare(a.at(), b.at()));
        pending.addAll(queue);
        while (!pending.isEmpty()) {
            Attempt attempt = pending.poll();
            double now = attempt.at();
            if (policy.breaker() && now < openUntil) {
                failed++;
                continue;
            }
            sent++;
            boolean outage = now >= 20 && now < 40;
            boolean ok = !outage && provider.tryTake(now, 1);
            if (ok) {
                succeeded++;
                failuresInARow = 0;
                waits.add(now - attempt.firstAt());
                continue;
            }
            failuresInARow++;
            if (policy.breaker() && failuresInARow >= 20) {
                openUntil = now + 5;
                failuresInARow = 0;
            }
            if (attempt.number() >= policy.maxAttempts()) {
                failed++;
                continue;
            }
            double delay = policy.backoff() ? Math.pow(2, attempt.number()) : 0.1;
            if (policy.jitter()) {
                delay = random.nextDouble() * delay;
            }
            pending.add(new Attempt(now + delay, attempt.number() + 1, attempt.firstAt()));
        }
        waits.sort(Double::compare);
        double p95 = waits.isEmpty() ? 0 : waits.get((int) (waits.size() * 0.95));
        return new Outcome(succeeded, failed, sent, p95);
    }

    /** What to do with each failure. The order matters: a spend cap is also a 429. */
    static String classify(int status, String errorCode, boolean hasRetryAfter) {
        if (status == 429 && "enforced_spend_limit_reached".equals(errorCode)) {
            return "STOP: spend cap, retrying fails until access resumes. Alert a person";
        }
        return switch (status) {
            case 400 -> "FIX: the request, or a spend limit you set. Never retry unchanged";
            case 401, 403 -> "FIX: credentials. Alert, and don't retry";
            case 404, 413 -> "FIX: model ID or size. Don't retry";
            case 429 -> hasRetryAfter ? "WAIT: for retry-after, then retry"
                    : "WAIT: with backoff, and queue or shed new work";
            case 500, 529 -> "RETRY: with backoff and jitter, then fall back";
            case 504 -> "RETRY: once, or stream the long request";
            default -> "LOG: and treat as not retryable";
        };
    }

    public static void main(String[] args) {
        System.out.println("A 20-second outage, 80 requests a second, capacity 100 a "
                + "second:");
        System.out.printf("%-34s %8s %7s %8s %8s %9s%n", "policy", "served", "failed",
                "sent", "amplify", "p95 wait");
        int offered = 80 * 120;
        for (Policy policy : List.of(
                new Policy("no retries", 1, false, false, false),
                new Policy("5 attempts, fixed 100 ms", 5, false, false, false),
                new Policy("5 attempts, backoff", 5, true, false, false),
                new Policy("5 attempts, backoff and jitter", 5, true, true, false),
                new Policy("same, plus a breaker", 5, true, true, true))) {
            Outcome o = simulate(policy);
            System.out.printf("%-34s %8d %7d %8d %7.2fx %8.1fs%n", policy.name(),
                    o.succeeded(), o.failed(), o.sent(), (double) o.sent() / offered,
                    o.p95Wait());
        }

        System.out.println();
        System.out.println("Retries multiply across layers (attempts at each layer):");
        for (int[] layers : new int[][] {{3}, {3, 3}, {3, 3, 3}, {3, 1, 1}}) {
            int product = 1;
            for (int layer : layers) {
                product *= layer;
            }
            System.out.println("  " + java.util.Arrays.toString(layers) + " -> up to "
                    + product + " calls per user request");
        }

        System.out.println();
        System.out.println("What to do with each error:");
        System.out.println("  429 retry-after    " + classify(429, null, true));
        System.out.println("  429 no header      " + classify(429, null, false));
        System.out.println("  429 spend cap      "
                + classify(429, "enforced_spend_limit_reached", false));
        System.out.println("  400                " + classify(400, null, false));
        System.out.println("  401                " + classify(401, null, false));
        System.out.println("  404                " + classify(404, null, false));
        System.out.println("  529                " + classify(529, null, false));
        System.out.println("  504                " + classify(504, null, false));

        System.out.println();
        System.out.println("Cache-aware input limit, with a 2,000,000 ITPM limit:");
        for (double hit : new double[] {0, 0.5, 0.8, 0.95}) {
            System.out.printf("  %3.0f%% cache reads -> %,12.0f total input tokens a "
                    + "minute%n",
                    hit * 100, 2_000_000 / (1 - hit));
        }

        System.out.println();
        System.out.println("A bucket lets a burst through, then holds the average:");
        TokenBucket bucket = new TokenBucket(10, 1);
        int admitted = 0;
        for (int i = 0; i < 30; i++) {
            admitted += bucket.tryTake(0, 1) ? 1 : 0;
        }
        System.out.println("  30 requests at t=0: admitted " + admitted);
        int later = 0;
        for (int second = 1; second <= 20; second++) {
            later += bucket.tryTake(second, 1) ? 1 : 0;
        }
        System.out.println("  one a second for 20 more seconds: admitted " + later);
    }
}
