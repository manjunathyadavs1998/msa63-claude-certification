package course.cc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI12 L3: a PreToolUse command hook. It reads the event as JSON on stdin. Exit 2 blocks
 * the
 * call, and stderr goes back to Claude. Run by HookLab through real processes, with
 * payloads
 * shaped from the docs. It hasn't been run inside a live Claude Code session.
 */
public final class ProtectFiles {

    private static final Pattern PATH =
            Pattern.compile("\"file_path\"\\s*:\\s*\"([^\"]*)\"");
    private static final Pattern COMMAND =
            Pattern.compile("\"command\"\\s*:\\s*\"([^\"]*)\"");

    private ProtectFiles() {
    }

    static boolean protectedPath(String text) {
        return text.contains(".env") || text.contains("src/gen/");
    }

    /** Mode "naive" crashes on bad input, and "safe" treats any failure as a block. */
    public static void main(String[] args) throws Exception {
        String mode = args.length > 0 ? args[0] : "safe";
        String input = new String(System.in.readAllBytes());
        try {
            if (!input.trim().startsWith("{")) {
                throw new IllegalArgumentException("not JSON");
            }
            Matcher path = PATH.matcher(input);
            String target = path.find() ? path.group(1) : "";
            Matcher command = COMMAND.matcher(input);
            String shell = command.find() ? command.group(1) : "";
            if (protectedPath(target) || (mode.equals("safe") && protectedPath(shell))) {
                System.err.println("Blocked: " + (target + shell) + " is protected.");
                System.exit(2);
            }
            System.exit(0);
        } catch (RuntimeException e) {
            if (mode.equals("naive")) {
                throw e;
            }
            System.err.println("Hook failed, so blocking to be safe: " + e.getMessage());
            System.exit(2);
        }
    }
}
