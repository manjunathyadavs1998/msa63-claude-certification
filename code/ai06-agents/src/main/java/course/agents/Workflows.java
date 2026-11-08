package course.agents;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

/**
 * AI06 L1: a prompt chain with a code gate, and a router. The "model" is a scripted
 * function, so this tests control flow only and says nothing about model quality.
 */
public final class Workflows {

    private Workflows() {
    }

    /** Runs each step on the last output. A failing gate gets one retry, then stops. */
    static List<String> chain(String input, List<Function<String, String>> steps,
            Function<String, String> gate) {
        List<String> trace = new ArrayList<>();
        String current = input;
        for (int i = 0; i < steps.size(); i++) {
            String output = steps.get(i).apply(current);
            String problem = gate.apply(output);
            if (problem != null) {
                trace.add("step " + (i + 1) + " rejected: " + problem);
                output = steps.get(i).apply(current + " [fix: " + problem + "]");
                problem = gate.apply(output);
                if (problem != null) {
                    trace.add("step " + (i + 1) + " failed twice, stopping");
                    return trace;
                }
            }
            trace.add("step " + (i + 1) + " ok: " + output);
            current = output;
        }
        return trace;
    }

    static String route(String ticket) {
        String t = ticket.toLowerCase(Locale.ROOT);
        if (t.contains("refund") || t.contains("charged")) {
            return "billing workflow";
        }
        if (t.contains("password") || t.contains("login")) {
            return "account workflow";
        }
        return "general agent";
    }

    public static void main(String[] args) {
        Function<String, String> outline = in -> in.contains("[fix")
                ? "Outline: scope, risks, rollout" : "Outline: scope";
        Function<String, String> draft = in -> "Draft from <" + in + ">";
        Function<String, String> gate = out -> out.equals("Outline: scope")
                ? "outline must list risks" : null;
        chain("Payment migration plan", List.of(outline, draft), gate)
                .forEach(System.out::println);
        for (String ticket : List.of("I was charged twice", "Login link expired",
                "My parcel arrived damaged and the invoice looks wrong")) {
            System.out.printf("%-18s <- %s%n", route(ticket), ticket);
        }
    }
}
