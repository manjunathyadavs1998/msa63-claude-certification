package course.cc;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI12 L5: the script around `claude -p`. It reads --output-format json output, whose shape
 * comes from the headless docs, and decides what the job does. The JSON is hand-written, as
 * no run spent real tokens. It's regex-parsed for a teaching sketch: use a JSON library.
 */
public final class CiFindings {

    record Finding(String file, int line, String rule, String severity, String snippet) {
        /** No line number: lines move between commits, and an issue must still match. */
        String fingerprint() {
            return file + "|" + rule + "|" + snippet.replaceAll("\\s+", " ").trim();
        }
    }

    record Decision(int exit, String message, List<Finding> toPost) {
    }

    private static final Pattern ITEM = Pattern.compile(
            "\\{\"file\":\"([^\"]+)\",\"line\":(\\d+),\"rule\":\"([^\"]+)\","
                    + "\"severity\":\"([^\"]+)\","
                    + "\"snippet\":\"((?:[^\"\\\\]|\\\\.)*)\"\\}");

    private CiFindings() {
    }

    static List<Finding> parse(String json) {
        List<Finding> out = new ArrayList<>();
        Matcher m = ITEM.matcher(json);
        while (m.find()) {
            out.add(new Finding(m.group(1), Integer.parseInt(m.group(2)), m.group(3),
                    m.group(4), m.group(5)));
        }
        return out;
    }

    /** A wrong reading of a failed run is "no findings", which would pass the job. */
    static Decision naive(String json) {
        List<Finding> findings = parse(json);
        return new Decision(0, findings.size() + " findings", findings);
    }

    static Decision decide(int processExit, String json, Set<String> alreadyPosted,
            Set<String> accepted) {
        if (processExit != 0) {
            return new Decision(2, "run failed, exit " + processExit
                    + ": review didn't happen", List.of());
        }
        if (!json.contains("\"structured_output\"")) {
            return new Decision(2, "no structured_output: review didn't happen", List.of());
        }
        List<Finding> fresh = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        for (Finding finding : parse(json)) {
            String print = finding.fingerprint();
            if (seen.add(print) && !alreadyPosted.contains(print)
                    && !accepted.contains(print)) {
                fresh.add(finding);
            }
        }
        boolean high = fresh.stream().anyMatch(f -> f.severity().equals("high"));
        return new Decision(high ? 1 : 0, fresh.size() + " new, "
                + (high ? "blocking" : "passing"), fresh);
    }

    private static String item(String file, int line, String rule, String severity,
            String snippet) {
        return "{\"file\":\"" + file + "\",\"line\":" + line + ",\"rule\":\"" + rule
                + "\",\"severity\":\"" + severity + "\",\"snippet\":\"" + snippet + "\"}";
    }

    private static String output(String... items) {
        return "{\"session_id\":\"s1\",\"structured_output\":{\"findings\":["
                + String.join(",", items) + "]}}";
    }

    public static void main(String[] args) {
        String sql = item("Pay.java", 40, "sql-concat", "high", "\\\"id=\\\" + id");
        String first = output(sql, item("Pay.java", 90, "log-pii", "medium", "log(email)"));
        String afterCommit = output(item("Pay.java", 52, "log-pii", "medium", "log(email)"),
                item("Ship.java", 7, "no-timeout", "medium", "client.send(r)"));
        String declined = "{\"session_id\":\"s3\",\"result\":\"I can't complete that.\"}";

        Decision one = decide(0, first, Set.of(), Set.of());
        System.out.println("first run: exit " + one.exit() + ", " + one.message());
        Set<String> posted = new LinkedHashSet<>();
        one.toPost().forEach(f -> posted.add(f.fingerprint()));

        Decision by = decide(0, afterCommit, Set.of(), Set.of());
        System.out.println("after a commit, no memory of prior findings: " + by.message()
                + " " + by.toPost().stream().map(Finding::rule).toList());
        Decision two = decide(0, afterCommit, posted, Set.of());
        System.out.println("after a commit, with prior findings: exit " + two.exit() + ", "
                + two.message() + " " + two.toPost().stream().map(Finding::rule).toList());
        Set<String> accepted = Set.of(one.toPost().get(0).fingerprint());
        Decision three = decide(0, first, Set.of(), accepted);
        System.out.println("high finding accepted by a person: exit " + three.exit() + ", "
                + three.message());

        System.out.println("run with no structured_output:");
        System.out.println("  naive script: exit " + naive(declined).exit() + ", "
                + naive(declined).message() + " (the job goes green)");
        Decision safe = decide(0, declined, Set.of(), Set.of());
        System.out.println("  this script:  exit " + safe.exit() + ", " + safe.message());
        Decision crash = decide(1, "", Set.of(), Set.of());
        System.out.println("process failed: exit " + crash.exit() + ", " + crash.message());
    }
}
