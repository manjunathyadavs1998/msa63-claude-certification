package course.cc;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * AI12 L3: runs the ProtectFiles hook as a real child process per event. The docs say exit
 * 2
 * blocks, and that most other non-zero exits let the action proceed.
 */
public final class HookLab {

    private HookLab() {
    }

    static int run(String mode, String stdin) throws Exception {
        Process process = new ProcessBuilder("java", "-cp", "out",
                "course.cc.ProtectFiles", mode)
                .redirectError(ProcessBuilder.Redirect.DISCARD).start();
        process.getOutputStream().write(stdin.getBytes(StandardCharsets.UTF_8));
        process.getOutputStream().close();
        return process.waitFor();
    }

    static String outcome(int exit) {
        return switch (exit) {
            case 0 -> "proceeds";
            case 2 -> "BLOCKED";
            default -> "proceeds (hook crashed, exit " + exit + ")";
        };
    }

    static String edit(String tool, String path) {
        return "{\"hook_event_name\":\"PreToolUse\",\"tool_name\":\"" + tool
                + "\",\"tool_input\":{\"file_path\":\"" + path + "\"}}";
    }

    static String bash(String command) {
        return "{\"hook_event_name\":\"PreToolUse\",\"tool_name\":\"Bash\","
                + "\"tool_input\":{\"command\":\"" + command + "\"}}";
    }

    public static void main(String[] args) throws Exception {
        record Case(String label, String payload) {
        }
        List<Case> cases = List.of(
                new Case("Edit src/App.java", edit("Edit", "src/App.java")),
                new Case("Edit .env", edit("Edit", ".env")),
                new Case("Write src/gen/Client.java", edit("Write", "src/gen/Client.java")),
                new Case("Bash: cat .env", bash("cat .env")),
                new Case("Bash: cat .e*", bash("cat .e*")),
                new Case("malformed input", "<<not json>>"));
        System.out.printf("%-28s %-34s %s%n", "event", "naive hook", "safe hook");
        for (Case c : cases) {
            System.out.printf("%-28s %-34s %s%n", c.label(), outcome(run("naive",
                    c.payload())),
                    outcome(run("safe", c.payload())));
        }

        List<Long> times = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            long start = System.nanoTime();
            run("safe", edit("Edit", "src/App.java"));
            times.add((System.nanoTime() - start) / 1_000_000);
        }
        Collections.sort(times);
        System.out.println();
        System.out.println("A Java hook starts a JVM on every tool call. Median on "
                + "this laptop: "
                + times.get(times.size() / 2) + " ms");
    }
}
