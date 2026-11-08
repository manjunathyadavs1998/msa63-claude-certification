package course.advanced;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;

/**
 * AI11 L5: a tiny durable workflow, crashed at every possible point, and compared with
 * naive
 * restarts. A teaching model of event-history replay. It isn't Temporal, and its "model" is
 * a random stand-in for one whose answers vary.
 */
public final class SagaLab {

    private SagaLab() {
    }

    static final class Crash extends RuntimeException {
    }

    /** The outside world. It remembers effects, so that duplicates can be counted. */
    static final class World {
        int charges;
        int refunds;
        int shipments;
        int emails;
        final Set<String> seenKeys = new HashSet<>();

        void charge(String key) {
            if (key == null || seenKeys.add("charge:" + key)) {
                charges++;
            }
        }

        void ship(String key) {
            if (key == null || seenKeys.add("ship:" + key)) {
                shipments++;
            }
        }

        void email(String key) {
            if (key == null || seenKeys.add("email:" + key)) {
                emails++;
            }
        }

        void refund(String key) {
            if (key == null || seenKeys.add("refund:" + key)) {
                refunds++;
            }
        }
    }

    /** Runs steps, stops at a chosen crash point, and records finished steps if durable. */
    static final class Engine {
        final Map<String, Object> history;
        final int crashAt;
        int point;

        Engine(Map<String, Object> history, int crashAt) {
            this.history = history;
            this.crashAt = crashAt;
        }

        private void maybeCrash() {
            if (point++ == crashAt) {
                throw new Crash();
            }
        }

        /** Crash points: before the effect, and between the effect and recording it. */
        @SuppressWarnings("unchecked")
        <T> T step(String name, Supplier<T> effect) {
            if (history != null && history.containsKey(name)) {
                return (T) history.get(name);
            }
            maybeCrash();
            T result = effect.get();
            maybeCrash();
            if (history != null) {
                history.put(name, result);
            }
            return result;
        }
    }

    /** Order flow: a model decides, then charge, ship, and notify. */
    static String workflow(Engine engine, World world, boolean useKeys,
            Supplier<String> model) {
        String decision = engine.step("decide", model);
        engine.step("charge", () -> {
            world.charge(useKeys ? "order-1" : null);
            return "ok";
        });
        engine.step("ship", () -> {
            world.ship(useKeys ? "order-1" : null);
            return "ok";
        });
        engine.step("email", () -> {
            world.email(useKeys ? "order-1" : null);
            return "ok";
        });
        return decision;
    }

    record Tally(int runs, int doubleCharges, int doubleShips, int doubleEmails,
            int correct) {
    }

    /** Crash once at each point, restart, and count what the world ended up with. */
    static Tally exhaustive(boolean durable, boolean useKeys) {
        int points = 8;
        int doubleCharges = 0;
        int doubleShips = 0;
        int doubleEmails = 0;
        int correct = 0;
        for (int crashAt = 0; crashAt < points; crashAt++) {
            World world = new World();
            Map<String, Object> history = durable ? new HashMap<>() : null;
            try {
                workflow(new Engine(history, crashAt), world, useKeys, () -> "approve");
            } catch (Crash expected) {
                workflow(new Engine(history, -1), world, useKeys, () -> "approve");
            }
            doubleCharges += world.charges > 1 ? 1 : 0;
            doubleShips += world.shipments > 1 ? 1 : 0;
            doubleEmails += world.emails > 1 ? 1 : 0;
            correct += world.charges == 1 && world.shipments == 1 && world.emails == 1 ? 1
                    : 0;
        }
        return new Tally(points, doubleCharges, doubleShips, doubleEmails, correct);
    }

    /** Replaying without recording the model's answer can change the decision. */
    static int divergences(boolean recordModel) {
        Random random = new Random(11);
        int changed = 0;
        int trials = 10_000;
        Supplier<String> model = () -> random.nextDouble() < 0.7 ? "approve" : "review";
        for (int i = 0; i < trials; i++) {
            Map<String, Object> history = new HashMap<>();
            String first;
            if (recordModel) {
                first = new Engine(history, -1).step("decide", model);
            } else {
                first = model.get();
            }
            String second = recordModel ? new Engine(history, -1).step("decide", model)
                    : model.get();
            changed += first.equals(second) ? 0 : 1;
        }
        return changed;
    }

    /** A saga: on failure, undo finished steps in reverse. Some can't be undone. */
    static List<String> saga(String failAt) {
        List<String> log = new ArrayList<>();
        Map<String, String> compensation = new LinkedHashMap<>();
        compensation.put("reserve stock", "release stock");
        compensation.put("charge card", "refund card");
        compensation.put("send email", "send a correction (the email can't be unsent)");
        compensation.put("ship parcel", "arrange a return");
        List<String> done = new ArrayList<>();
        for (String step : compensation.keySet()) {
            if (step.equals(failAt)) {
                log.add(step + " FAILED");
                for (int i = done.size() - 1; i >= 0; i--) {
                    log.add("undo: " + compensation.get(done.get(i)));
                }
                return log;
            }
            log.add(step);
            done.add(step);
        }
        log.add("complete");
        return log;
    }

    /** At-least-once delivery means duplicates, so the consumer has to absorb them. */
    static void delivery() {
        Random random = new Random(5);
        int events = 100_000;
        int delivered = 0;
        Set<Integer> seen = new HashSet<>();
        int processedNaive = 0;
        int processedDeduped = 0;
        for (int id = 0; id < events; id++) {
            int copies = random.nextDouble() < 0.02 ? 2 : 1;
            for (int c = 0; c < copies; c++) {
                delivered++;
                processedNaive++;
                if (seen.add(id)) {
                    processedDeduped++;
                }
            }
        }
        System.out.printf("  %,d events, delivered %,d times at a 2%% duplicate rate%n",
                events,
                delivered);
        System.out.printf("  no dedupe: %,d model calls. With a seen-ID check: %,d. "
                + "Waste avoided"
                + " at $0.013 each: $%,.0f%n", processedNaive, processedDeduped,
                (processedNaive - processedDeduped) * 0.013);
    }

    public static void main(String[] args) {
        System.out.println("One crash at each of 8 points, then a restart:");
        System.out.printf("  %-34s %7s %7s %7s %8s%n", "design", "2xcharge", "2xship",
                "2xemail",
                "correct");
        for (Object[] row : new Object[][] {
                {"restart from the top", false, false},
                {"restart, with idempotency keys", false, true},
                {"durable history, no keys", true, false},
                {"durable history, with keys", true, true}}) {
            Tally t = exhaustive((boolean) row[1], (boolean) row[2]);
            System.out.printf("  %-34s %7d %7d %7d %5d/%d%n", row[0], t.doubleCharges(),
                    t.doubleShips(), t.doubleEmails(), t.correct(), t.runs());
        }

        System.out.println();
        System.out.println("Replaying a run after a crash, with a model that varies, "
                + "10,000 times:");
        System.out.println("  model answer not recorded: decision changed "
                + divergences(false) + " times");
        System.out.println("  model answer recorded:     decision changed "
                + divergences(true) + " times");

        System.out.println();
        System.out.println("Saga, when a step fails:");
        for (String failAt : new String[] {"charge card", "ship parcel"}) {
            System.out.println("  " + saga(failAt));
        }

        System.out.println();
        System.out.println("At-least-once delivery:");
        delivery();
    }
}
