package course.prod;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * AI10 L4 and L6: tracing arithmetic, sampling, drift, fair sharing, canaries and failover.
 * Everything is a simulation on invented traffic, repeatable from a seed.
 */
public final class OpsLab {

    private OpsLab() {
    }

    record Span(String name, int depth, long millis, long input, long read, long output,
            String note) {
    }

    /** A trace of one invented agent run, as a backend might display it. */
    static List<Span> trace() {
        return List.of(
                new Span("agent.run", 0, 41_200, 0, 0, 0, ""),
                new Span("model turn 1", 1, 2_100, 1_200, 8_000, 90, "cache hit"),
                new Span("tool search_orders", 1, 180, 0, 0, 0, ""),
                new Span("model turn 2", 1, 2_400, 900, 8_000, 120, "cache hit"),
                new Span("tool get_invoice", 1, 30_000, 0, 0, 0, "TIMEOUT, retried"),
                new Span("tool get_invoice", 1, 240, 0, 0, 0, ""),
                new Span("model turn 3", 1, 3_300, 14_000, 0, 200,
                        "cache MISS, prefix changed"),
                new Span("tool get_invoice", 1, 220, 0, 0, 0, "same call repeated"),
                new Span("model turn 4", 1, 2_760, 1_100, 14_000, 310, "cache hit"));
    }

    static void printTrace() {
        double inputPrice = 2.0;
        double readPrice = 0.1;
        double outputPrice = 10.0;
        double total = 0;
        for (Span span : trace()) {
            double cost = (span.input() * inputPrice + span.read() * readPrice
                    + span.output() * outputPrice) / 1e6;
            total += cost;
            System.out.printf("  %s%-22s %6d ms  in %6d  read %6d  out %4d  $%.4f  %s%n",
                    "  ".repeat(span.depth()), span.name(), span.millis(), span.input(),
                    span.read(), span.output(), cost, span.note());
        }
        System.out.printf("  total cost $%.4f%n", total);
    }

    /** Head sampling decides before the outcome is known, and tail sampling after. */
    static void sampling() {
        Random random = new Random(3);
        int traces = 1_000_000;
        int errors = 0;
        int slow = 0;
        int headKept = 0;
        int headErrors = 0;
        int tailKept = 0;
        int tailErrors = 0;
        for (int i = 0; i < traces; i++) {
            boolean error = random.nextDouble() < 0.002;
            boolean isSlow = random.nextDouble() < 0.01;
            errors += error ? 1 : 0;
            slow += isSlow ? 1 : 0;
            boolean head = random.nextDouble() < 0.01;
            headKept += head ? 1 : 0;
            headErrors += head && error ? 1 : 0;
            boolean tail = error || isSlow || random.nextDouble() < 0.001;
            tailKept += tail ? 1 : 0;
            tailErrors += tail && error ? 1 : 0;
        }
        System.out.printf("  %,d traces, %,d errors, %,d slow%n", traces, errors, slow);
        System.out.printf("  1%% head sampling: keep %,d traces, including %,d errors%n",
                headKept,
                headErrors);
        System.out.printf("  tail sampling:    keep %,d traces, including %,d errors%n",
                tailKept,
                tailErrors);
    }

    /** The mean hides the tail. Latency here is a mix of two invented groups. */
    static void percentiles() {
        Random random = new Random(5);
        List<Double> values = new ArrayList<>();
        for (int i = 0; i < 100_000; i++) {
            boolean agent = random.nextDouble() < 0.1;
            double median = agent ? 25 : 2;
            values.add(median * Math.exp(random.nextGaussian() * 0.5));
        }
        values.sort(Double::compare);
        double mean = values.stream().mapToDouble(v -> v).average().orElse(0);
        System.out.printf("  mean %.1f s, p50 %.1f s, p95 %.1f s, p99 %.1f s%n", mean,
                values.get(50_000), values.get(95_000), values.get(99_000));
    }

    /** Population stability index: a simple alarm for a change in what users ask. */
    static double psi(double[] baseline, double[] current) {
        double sum = 0;
        for (int i = 0; i < baseline.length; i++) {
            sum += (current[i] - baseline[i]) * Math.log(current[i] / baseline[i]);
        }
        return sum;
    }

    /** One provider limit, shared. Tenant A floods, and B and C behave. */
    static void fairness(boolean perTenant) {
        Map<String, Deque<Integer>> queues = new LinkedHashMap<>();
        for (String tenant : List.of("A-flood", "B", "C")) {
            queues.put(tenant, new ArrayDeque<>());
        }
        Deque<Object[]> fifo = new ArrayDeque<>();
        Map<String, List<Integer>> waits = new LinkedHashMap<>();
        queues.keySet().forEach(tenant -> waits.put(tenant, new ArrayList<>()));
        int capacity = 100;
        for (int second = 0; second < 120; second++) {
            if (second < 60) {
                add(queues, fifo, "A-flood", second, 300, perTenant);
            }
            add(queues, fifo, "B", second, 20, perTenant);
            add(queues, fifo, "C", second, 20, perTenant);
            int served = 0;
            if (perTenant) {
                while (served < capacity && queues.values().stream()
                        .anyMatch(q -> !q.isEmpty())) {
                    for (Map.Entry<String, Deque<Integer>> entry : queues.entrySet()) {
                        if (served < capacity && !entry.getValue().isEmpty()) {
                            waits.get(entry.getKey()).add(second - entry.getValue().poll());
                            served++;
                        }
                    }
                }
            } else {
                while (served < capacity && !fifo.isEmpty()) {
                    Object[] item = fifo.poll();
                    waits.get((String) item[0]).add(second - (Integer) item[1]);
                    served++;
                }
            }
        }
        waits.forEach((tenant, list) -> {
            list.sort(Integer::compare);
            System.out.printf("    %-8s served %6d, median wait %3d s, p95 wait %3d s%n",
                    tenant,
                    list.size(), list.isEmpty() ? 0 : list.get(list.size() / 2),
                    list.isEmpty() ? 0 : list.get((int) (list.size() * 0.95)));
        });
    }

    private static void add(Map<String, Deque<Integer>> queues, Deque<Object[]> fifo,
            String tenant, int second, int count, boolean perTenant) {
        for (int i = 0; i < count; i++) {
            if (perTenant) {
                queues.get(tenant).add(second);
            } else {
                fifo.add(new Object[] {tenant, second});
            }
        }
    }

    /** Requests per arm to detect a drop in a rate, at 95% confidence and 80% power. */
    static long sampleSize(double base, double drop) {
        double p2 = base - drop;
        double variance = base * (1 - base) + p2 * (1 - p2);
        return Math.round(Math.pow(1.96 + 0.84, 2) * variance / (drop * drop));
    }

    /** Serial dependencies multiply. A fallback helps only if failures are independent. */
    static void availability() {
        double provider = 0.999;
        double gateway = 0.9995;
        double vectorDb = 0.999;
        System.out.printf("  provider %.2f%%, gateway %.2f%%, vector DB %.2f%% in "
                + "series: %.3f%%%n",
                provider * 100, gateway * 100, vectorDb * 100,
                100 * provider * gateway * vectorDb);
        double independent = 1 - (1 - provider) * (1 - provider);
        System.out.printf("  two providers, independent: %.5f%%%n", independent * 100);
        double correlated = 0.2;
        double both = (1 - provider) * (correlated + (1 - correlated) * (1 - provider));
        System.out.printf("  two providers, 20%% of outages shared: %.4f%%%n",
                100 * (1 - both));
        for (double available : new double[] {0.99, 0.999, 0.9999}) {
            System.out.printf("  %.2f%% is %,.0f minutes down a year%n", available * 100,
                    (1 - available) * 365 * 24 * 60);
        }
    }

    /** Failover moves load, so the survivor needs room for it. */
    static void failover() {
        double load = 1_500_000;
        for (double reserve : new double[] {1_000_000, 2_000_000, 3_000_000}) {
            double normal = load / 2;
            double served = Math.min(load, reserve);
            System.out.printf("  each provider limit %,.0f ITPM, normal share %,.0f:"
                    + " after failover serves %3.0f%%%n", reserve, normal,
                    100 * served / load);
        }
    }

    public static void main(String[] args) {
        System.out.println("One agent run:");
        printTrace();

        System.out.println();
        System.out.println("Sampling:");
        sampling();

        System.out.println();
        System.out.println("Latency mix of 90% chat and 10% agent runs:");
        percentiles();

        System.out.println();
        System.out.println("Drift in the share of question types:");
        double[] baseline = {0.50, 0.30, 0.15, 0.05};
        System.out.printf("  small change PSI %.3f%n",
                psi(baseline, new double[] {0.48, 0.31, 0.16, 0.05}));
        System.out.printf("  new topic PSI %.3f%n",
                psi(baseline, new double[] {0.30, 0.20, 0.10, 0.40}));

        System.out.println();
        System.out.println("One shared limit of 100/s. A floods at 300/s for a minute:");
        System.out.println("  one shared queue:");
        fairness(false);
        System.out.println("  a queue per tenant, served in turn:");
        fairness(true);

        System.out.println();
        System.out.println("Requests per arm to detect a drop (95% confidence, 80% "
                + "power):");
        for (double[] row : new double[][] {{0.95, 0.05}, {0.95, 0.02}, {0.95, 0.01},
                {0.90, 0.02}, {0.99, 0.005}}) {
            System.out.printf("  from %.1f%%, a drop of %.1f points: %,d%n", row[0] * 100,
                    row[1] * 100, sampleSize(row[0], row[1]));
        }

        System.out.println();
        System.out.println("Availability, illustrative figures:");
        availability();

        System.out.println();
        System.out.println("Failover with 1,500,000 ITPM of total load:");
        failover();

        System.out.println();
        LocalDate today = LocalDate.of(2026, 11, 5);
        LocalDate retires = LocalDate.of(2026, 11, 30);
        System.out.println("Days from 2026-11-05 to Sonnet 4.5's published retirement: "
                + ChronoUnit.DAYS.between(today, retires));
    }
}
