package course.cc;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * AI12 L2 and L3: flags Claude Code configuration mistakes that fail silently. Each check
 * comes from a statement in the docs, for example that unknown frontmatter fields are
 * ignored without an error. Java's glob matcher approximates Claude Code's.
 */
public final class ConfigLint {

    private static final Set<String> SKILL_FIELDS = Set.of("name", "description",
            "when_to_use",
            "disable-model-invocation", "user-invocable", "allowed-tools", "context",
                    "agent",
            "background", "argument-hint", "arguments", "model", "effort", "paths", "hooks",
            "shell", "version", "license", "compatibility", "metadata");
    private static final Set<String> AGENT_FIELDS = Set.of("name", "description", "tools",
            "disallowedTools", "model", "permissionMode", "skills", "hooks", "memory",
            "isolation", "mcpServers", "maxTurns", "background", "effort", "color",
            "initialPrompt");
    private static final Pattern SECRET =
            Pattern.compile("(ghp_|sk-ant-|xoxb-|AKIA)[A-Za-z0-9_-]+");
    private static final Pattern FRONT = Pattern.compile("(?s)\\A---\\R(.*?)\\R---");

    private ConfigLint() {
    }

    static List<String> keys(String text) {
        List<String> keys = new ArrayList<>();
        Matcher front = FRONT.matcher(text);
        if (front.find()) {
            for (String line : front.group(1).split("\\R")) {
                Matcher key = Pattern.compile("^([A-Za-z_-]+)\\s*:").matcher(line);
                if (key.find()) {
                    keys.add(key.group(1));
                }
            }
        }
        return keys;
    }

    static List<String> globs(String text) {
        List<String> out = new ArrayList<>();
        Matcher front = FRONT.matcher(text);
        if (front.find()) {
            Matcher quoted = Pattern.compile("\"([^\"]+)\"").matcher(front.group(1));
            while (quoted.find()) {
                out.add(quoted.group(1));
            }
        }
        return out;
    }

    static List<Path> walk(Path root) throws Exception {
        try (Stream<Path> stream = Files.walk(root)) {
            return stream.filter(Files::isRegularFile).toList();
        }
    }

    static List<String> lint(Path root) throws Exception {
        List<String> out = new ArrayList<>();
        List<Path> files = walk(root);
        for (Path file : files) {
            String rel = root.relativize(file).toString().replace('\\', '/');
            String name = file.getFileName().toString();
            if (rel.contains("/.git/") || rel.startsWith(".git/")) {
                continue;
            }
            if (name.equals("CLAUDE.md") || name.equals("CLAUDE.local.md")) {
                List<String> lines = Files.readAllLines(file);
                if (lines.size() > 200) {
                    out.add(rel + ": " + lines.size() + " lines. The docs target under 200,"
                            + " so move parts to .claude/rules/ or skills");
                }
                long loud = lines.stream().filter(l
                        -> l.matches(".*\\b(MUST|NEVER|ALWAYS)\\b.*"))
                        .count();
                if (loud >= 3) {
                    out.add(rel + ": " + loud + " emphatic rules. If they must always hold,"
                            + " use hooks or deny rules, since this file is only context");
                }
            }
            if (name.equals("CLAUDE.local.md")) {
                Path ignore = root.resolve(".gitignore");
                if (!Files.exists(ignore)
                        || !Files.readString(ignore).contains("CLAUDE.local.md")) {
                    out.add(rel + ": personal file not in .gitignore, so it may be "
                            + "committed");
                }
            }
            if (name.equals(".mcp.json")) {
                String text = Files.readString(file);
                Matcher secret = SECRET.matcher(text);
                if (secret.find()) {
                    out.add(rel + ": looks like a committed secret. Use ${VAR} expansion");
                }
            }
            if (rel.startsWith(".claude/rules/") && name.endsWith(".md")) {
                String text = Files.readString(file);
                List<String> keys = keys(text);
                for (String key : keys) {
                    if (!key.equals("paths")) {
                        out.add(rel + ": field '" + key
                                + "' is ignored, as only paths is read."
                                + " The rule loads in every session");
                    }
                }
                for (String glob : globs(text)) {
                    PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:"
                            + glob);
                    boolean any = files.stream().anyMatch(f
                            -> matcher.matches(root.relativize(f)));
                    if (!any) {
                        out.add(rel + ": glob " + glob + " matches no file here");
                    }
                }
            }
            if (rel.startsWith(".claude/skills/") && name.equals("SKILL.md")) {
                String text = Files.readString(file);
                List<String> keys = keys(text);
                if (!keys.contains("description")) {
                    out.add(rel + ": no description, so Claude can't tell when it applies");
                }
                for (String key : keys) {
                    if (!SKILL_FIELDS.contains(key)) {
                        out.add(rel + ": unknown field '" + key + "' in frontmatter");
                    }
                }
                if (keys.contains("allowed-tools")) {
                    out.add(rel + ": note: allowed-tools pre-approves tools for that turn."
                            + " It doesn't block others, so use deny rules for that");
                }
            }
            if (rel.startsWith(".claude/agents/") && name.endsWith(".md")) {
                String text = Files.readString(file);
                for (String key : keys(text)) {
                    if (!AGENT_FIELDS.contains(key)) {
                        out.add(rel + ": field '" + key
                                + "' is ignored without an error, so"
                                + " the restriction you meant doesn't apply");
                    }
                }
            }
        }
        return out;
    }

    static void write(Path root, String rel, String text) throws Exception {
        Path file = root.resolve(rel);
        Files.createDirectories(file.getParent());
        Files.writeString(file, text);
    }

    static Path fixture() throws Exception {
        Path root = Files.createTempDirectory("sample-project");
        write(root, "src/Button.test.tsx", "// test\n");
        write(root, "src/api/orders.ts", "// api\n");
        write(root, "CLAUDE.md", "# Project\n" + "- filler line\n".repeat(260)
                + "NEVER edit src/gen.\nALWAYS run tests.\nYou MUST add a licence "
                        + "header.\n");
        write(root, "CLAUDE.local.md", "Use my sandbox.\n");
        write(root, ".mcp.json", "{\"mcpServers\":{\"github\":{\"env\":{\"GITHUB_TOKEN\":"
                + "\"ghp_abc123fakeexample\"}}}}\n");
        write(root, ".claude/rules/tests.md",
                "---\nglobs: \"**/*.test.tsx\"\n---\nTest rules.\n");
        write(root, ".claude/rules/spec.md",
                "---\npaths:\n  - \"**/*.spec.ts\"\n---\nSpec rules.\n");
        write(root, ".claude/rules/api.md",
                "---\npaths:\n  - \"src/api/**\"\n---\nAPI rules.\n");
        write(root, ".claude/skills/analyze/SKILL.md", "---\ncontexts: fork\n"
                + "allowed-tools: Read\n---\nAnalyze the code.\n");
        write(root, ".claude/agents/reader.md",
                "---\nname: reader\ndescription: Reads code\n"
                + "allowedTools: Read, Grep\n---\nRead only.\n");
        return root;
    }

    public static void main(String[] args) throws Exception {
        System.out.println("A sample project with deliberate mistakes:");
        lint(fixture()).stream().sorted().forEach(line -> System.out.println("  " + line));
        if (args.length > 0) {
            System.out.println();
            System.out.println("This course's own repository:");
            List<String> real = lint(Path.of(args[0]));
            if (real.isEmpty()) {
                System.out.println("  no findings");
            }
            real.forEach(line -> System.out.println("  " + line));
        }
    }
}
