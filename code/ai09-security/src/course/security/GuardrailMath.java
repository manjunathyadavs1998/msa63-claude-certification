package course.security;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI09 L5: what a screen's rates mean at real traffic, and an output check that compares
 * figures in an answer with tool results. All rates here are illustrative inputs.
 */
public final class GuardrailMath {

    private GuardrailMath() {
    }

    static void screen(String name, double attackShare, double recall, double falseAlarm) {
        double traffic = 1_000_000;
        double attacks = traffic * attackShare;
        double caught = attacks * recall;
        double alarms = (traffic - attacks) * falseAlarm;
        System.out.printf("%-26s attacks %5.0f, missed %4.0f, innocent blocked %5.0f,"
                + " flags that are real %4.1f%%%n", name, attacks, attacks - caught, alarms,
                100 * caught / (caught + alarms));
    }

    private static final Pattern FIGURE =
            Pattern.compile("(?<![\\w.])\\d[\\d,]*(?:\\.\\d+)?");

    private static List<String> figures(String text) {
        List<String> out = new ArrayList<>();
        Matcher matcher = FIGURE.matcher(text);
        while (matcher.find()) {
            out.add(matcher.group().replace(",", ""));
        }
        return out;
    }

    /** Every figure in the answer must appear in a tool result. It checks numbers only. */
    static List<String> unsupportedFigures(String answer, List<String> toolResults) {
        Set<String> evidence = new HashSet<>();
        toolResults.forEach(result -> evidence.addAll(figures(result)));
        return figures(answer).stream().filter(f -> !evidence.contains(f)).toList();
    }

    public static void main(String[] args) {
        System.out.println("A million messages, illustrative rates:");
        screen("rare attacks, good screen", 0.001, 0.95, 0.01);
        screen("rare attacks, strict", 0.001, 0.99, 0.05);
        screen("rare attacks, lenient", 0.001, 0.80, 0.001);
        screen("common attacks", 0.05, 0.95, 0.01);

        System.out.println();
        System.out.println("Two screens, each missing 5%:");
        System.out.printf("  missed by both, if independent: %.2f%%%n", 100 * 0.05 * 0.05);
        System.out.println("  missed by both, if they always fail together: 5.00%");

        System.out.println();
        List<String> results = List.of("{\"total\":\"41.30\",\"transactions\":[\"t1\"]}");
        for (String answer : List.of("You spent 41.30 on groceries.",
                "You spent 43.10 on groceries.", "You spent 1.30 on groceries.",
                "You spent about forty dollars.",
                "You spent 41.30, and you should cancel your card.")) {
            System.out.printf("%-52s unsupported: %s%n", answer,
                    unsupportedFigures(answer, results));
        }
    }
}
