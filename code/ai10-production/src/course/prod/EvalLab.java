package course.prod;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * AI10 L3: evaluation statistics on synthetic extraction results, generated from a seed.
 * The pipeline's accuracy is an input I chose, so the lab demonstrates methods. It measures
 * no model.
 */
public final class EvalLab {

    private EvalLab() {
    }

    record Item(String docType, String field, double confidence, boolean correct) {
    }

    /** Invented truth: invoices are easy, handwritten forms hide a weak field. */
    static List<Item> generate(long seed, int total) {
        Random random = new Random(seed);
        List<Item> items = new ArrayList<>();
        for (int i = 0; i < total; i++) {
            double type = random.nextDouble();
            String doc = type < 0.85 ? "invoice" : type < 0.97 ? "receipt" : "handwritten";
            String field = switch (random.nextInt(3)) {
                case 0 -> "total";
                case 1 -> "date";
                default -> "tax_id";
            };
            double accuracy = switch (doc) {
                case "invoice" -> 0.995;
                case "receipt" -> 0.97;
                default -> field.equals("tax_id") ? 0.45 : 0.80;
            };
            boolean correct = random.nextDouble() < accuracy;
            // Stated confidence is informative, and it isn't a probability.
            double base = correct ? 0.93 : 0.82;
            double confidence = Math.min(0.999, Math.max(0.3,
                    base + random.nextGaussian() * 0.06));
            items.add(new Item(doc, field, confidence, correct));
        }
        return items;
    }

    static double accuracy(List<Item> items) {
        return items.stream().filter(Item::correct).count() / (double) items.size();
    }

    /** Wilson interval, which behaves sensibly for small samples and rates near 0 or 1. */
    static double[] wilson(int successes, int n) {
        double z = 1.96;
        double p = (double) successes / n;
        double denominator = 1 + z * z / n;
        double centre = (p + z * z / (2 * n)) / denominator;
        double margin = z * Math.sqrt(p * (1 - p) / n
                + z * z / (4.0 * n * n)) / denominator;
        return new double[] {centre - margin, centre + margin};
    }

    static void segments(List<Item> items) {
        Map<String, List<Item>> groups = new LinkedHashMap<>();
        for (Item item : items) {
            groups.computeIfAbsent(item.docType() + "/" + item.field(),
                    k -> new ArrayList<>()).add(item);
        }
        groups.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
            List<Item> group = entry.getValue();
            int ok = (int) group.stream().filter(Item::correct).count();
            double[] ci = wilson(ok, group.size());
            System.out.printf("  %-22s n=%5d  accuracy %5.1f%%  95%% CI %5.1f-%5.1f%%%n",
                    entry.getKey(), group.size(), 100.0 * ok / group.size(), 100 * ci[0],
                    100 * ci[1]);
        });
    }

    /** Stated confidence against the truth, on a labelled validation set. */
    static void calibration(List<Item> validation) {
        double[] edges = {0, 0.7, 0.8, 0.85, 0.9, 0.95, 1.0001};
        double weightedGap = 0;
        for (int b = 0; b < edges.length - 1; b++) {
            double low = edges[b];
            double high = edges[b + 1];
            List<Item> bin = validation.stream()
                    .filter(i -> i.confidence() >= low && i.confidence() < high).toList();
            if (bin.isEmpty()) {
                continue;
            }
            double stated = bin.stream().mapToDouble(Item::confidence).average().orElse(0);
            double real = accuracy(bin);
            weightedGap += Math.abs(stated - real) * bin.size() / validation.size();
            System.out.printf("  stated %3.0f-%3.0f%%  n=%5d  avg stated %5.1f%%  "
                    + "real %5.1f%%%n",
                    low * 100, Math.min(high, 1) * 100, bin.size(), stated * 100,
                            real * 100);
        }
        System.out.printf("  expected calibration error: %.1f points%n", 100 * weightedGap);
    }

    /** The lowest threshold at which accepted items reach the target, on validation data.
     * */
    static double chooseThreshold(List<Item> validation, double target) {
        for (double t = 0.5; t < 1; t += 0.005) {
            double threshold = t;
            List<Item> accepted =
                    validation.stream().filter(i -> i.confidence() >= threshold).toList();
            if (accepted.size() >= 200 && accuracy(accepted) >= target) {
                return threshold;
            }
        }
        return 1;
    }

    static void routing(String label, List<Item> test, double threshold) {
        List<Item> accepted = test.stream().filter(i -> i.confidence() >=
                threshold).toList();
        List<Item> weak = accepted.stream().filter(i -> i.docType().equals("handwritten")
                && i.field().equals("tax_id")).toList();
        System.out.printf("  %-30s threshold %.3f: auto %4.1f%%, accepted accuracy %5.2f%%,"
                + " handwritten tax_id %s%n", label, threshold,
                100.0 * accepted.size() / test.size(), 100 * accuracy(accepted),
                weak.isEmpty() ? "none" : String.format("%.1f%% (n=%d)",
                        100 * accuracy(weak), weak.size()));
    }

    /** Equal quota per segment. Random sampling rarely reaches the small ones. */
    static void sampling(List<Item> highConfidence) {
        Random random = new Random(7);
        int budget = 300;
        List<Item> shuffled = new ArrayList<>(highConfidence);
        java.util.Collections.shuffle(shuffled, random);
        List<Item> simple = shuffled.subList(0, budget);
        Map<String, Integer> quota = new LinkedHashMap<>();
        List<Item> stratified = new ArrayList<>();
        for (Item item : shuffled) {
            String key = item.docType();
            if (quota.getOrDefault(key, 0) < budget / 3) {
                quota.merge(key, 1, Integer::sum);
                stratified.add(item);
            }
        }
        for (String doc : List.of("invoice", "receipt", "handwritten")) {
            List<Item> a = simple.stream().filter(i -> i.docType().equals(doc)).toList();
            List<Item> b = stratified.stream().filter(i
                    -> i.docType().equals(doc)).toList();
            List<Item> truth = highConfidence.stream().filter(i -> i.docType().equals(doc))
                    .toList();
            System.out.printf("  %-12s true %5.1f%% | random n=%3d est %5s | "
                    + "stratified n=%3d"
                    + " est %5.1f%%%n", doc, 100 * accuracy(truth), a.size(),
                    a.isEmpty() ? "none" : String.format("%.1f%%", 100 * accuracy(a)),
                            b.size(),
                    100 * accuracy(b));
        }
    }

    /** Pass rates vary by chance. How much of a drop can a suite of this size detect? */
    static void suiteSize() {
        System.out.println("95% interval for a measured 90% pass rate:");
        for (int n : new int[] {20, 50, 100, 200, 500, 1000}) {
            double[] ci = wilson((int) Math.round(n * 0.9), n);
            System.out.printf("  %5d tasks: %5.1f-%5.1f%% (width %4.1f points)%n", n,
                    100 * ci[0], 100 * ci[1], 100 * (ci[1] - ci[0]));
        }
    }

    /** With a noisy model, a fixed gate fails good releases and passes bad ones. */
    static void gate(int tasks, double threshold) {
        Random random = new Random(11);
        int runs = 10_000;
        for (double trueRate : new double[] {0.92, 0.90, 0.88, 0.85}) {
            int passed = 0;
            for (int run = 0; run < runs; run++) {
                int ok = 0;
                for (int t = 0; t < tasks; t++) {
                    ok += random.nextDouble() < trueRate ? 1 : 0;
                }
                passed += (double) ok / tasks >= threshold ? 1 : 0;
            }
            System.out.printf("  true rate %2.0f%%: gate passes %5.1f%% of runs%n",
                    trueRate * 100, 100.0 * passed / runs);
        }
    }

    /** Agreement beyond chance between a model judge and human labels. */
    static double kappa(int bothYes, int bothNo, int judgeOnly, int humanOnly) {
        double n = bothYes + bothNo + judgeOnly + humanOnly;
        double observed = (bothYes + bothNo) / n;
        double yes = (bothYes + judgeOnly) / n * (bothYes + humanOnly) / n;
        double no = (bothNo + humanOnly) / n * (bothNo + judgeOnly) / n;
        double chance = yes + no;
        return (observed - chance) / (1 - chance);
    }

    public static void main(String[] args) {
        List<Item> all = generate(2026, 40_000);
        List<Item> validation = all.subList(0, 20_000);
        List<Item> test = all.subList(20_000, 40_000);

        System.out.printf("Overall accuracy on 20,000 test fields: %.1f%%%n",
                100 * accuracy(test));
        System.out.println("By segment:");
        segments(test);

        System.out.println();
        System.out.println("Calibration on the validation set:");
        calibration(validation);

        System.out.println();
        System.out.println("Routing by confidence, threshold chosen on validation only:");
        routing("naive: trust 90% as stated", test, 0.90);
        double target = chooseThreshold(validation, 0.995);
        routing("calibrated for 99.5% accepted", test, target);
        List<Item> withoutWeak = test.stream()
                .filter(i -> !i.docType().equals("handwritten")).toList();
        routing("same, handwritten sent to people", withoutWeak, target);

        System.out.println();
        System.out.println("Sampling 300 high-confidence fields for human audit:");
        sampling(test.stream().filter(i -> i.confidence() >= target).toList());

        System.out.println();
        suiteSize();

        System.out.println();
        System.out.println("A gate of 90% on 50 tasks, run 10,000 times:");
        gate(50, 0.90);
        System.out.println("The same gate on 500 tasks:");
        gate(500, 0.90);

        System.out.println();
        System.out.printf("Judge against humans, 200 answers: raw agreement 90.0%%, "
                + "kappa %.2f%n",
                kappa(170, 10, 15, 5));
        System.out.printf("Judge against humans, balanced:     raw agreement 90.0%%, "
                + "kappa %.2f%n",
                kappa(90, 90, 10, 10));
    }
}
