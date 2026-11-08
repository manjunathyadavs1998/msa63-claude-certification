package course.cc;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * AI12 L1: what Grep-first exploration saves, measured on this course's own AI08 project.
 * Tokens are estimated as characters / 4, which is a rough rule and not a tokenizer. It
 * also shows how Edit's uniqueness rule plays out on a real file.
 */
public final class ExploreCost {

    private ExploreCost() {
    }

    static long tokens(long chars) {
        return chars / 4;
    }

    static List<Path> javaFiles(Path root) throws Exception {
        try (Stream<Path> walk = Files.walk(root)) {
            return walk.filter(p -> p.toString().endsWith(".java")).toList();
        }
    }

    /** One line per match, as a content search would show. */
    static List<String> grep(List<Path> files, Pattern pattern) throws Exception {
        List<String> hits = new ArrayList<>();
        for (Path file : files) {
            int n = 0;
            for (String line : Files.readAllLines(file)) {
                n++;
                if (pattern.matcher(line).find()) {
                    hits.add(file.getFileName() + ":" + n + ": " + line.strip());
                }
            }
        }
        return hits;
    }

    static int occurrences(String text, String target) {
        int count = 0;
        int at = text.indexOf(target);
        while (at >= 0) {
            count++;
            at = text.indexOf(target, at + 1);
        }
        return count;
    }

    public static void main(String[] args) throws Exception {
        Path root = Path.of(args[0]);
        List<Path> files = javaFiles(root);
        long all = 0;
        for (Path file : files) {
            all += Files.size(file);
        }
        System.out.printf("AI08 project: %d Java files, about %,d tokens%n", files.size(),
                tokens(all));

        Pattern pattern = Pattern.compile("RefundPolicy");
        List<String> hits = grep(files, pattern);
        long hitChars = hits.stream().mapToLong(String::length).sum();
        long readHits = 0;
        int hitFiles = 0;
        for (Path file : files) {
            String text = Files.readString(file);
            if (pattern.matcher(text).find()) {
                readHits += text.length();
                hitFiles++;
            }
        }
        System.out.println("Question: where is RefundPolicy used?");
        System.out.printf("  read every file:          about %,6d tokens%n", tokens(all));
        System.out.printf("  grep only (%2d lines):      about %,6d tokens%n", hits.size(),
                tokens(hitChars));
        System.out.printf("  grep, then read %d files:   about %,6d tokens%n", hitFiles,
                tokens(hitChars + readHits));

        long testFiles = files.stream().filter(p -> p.getFileName().toString()
                .endsWith("Test.java")).count();
        long nameChars = files.stream().mapToLong(p -> p.toString().length()).sum();
        System.out.printf("Question: which files are tests? A path match needs no file"
                + " contents:%n  %d of %d files, about %,d tokens of paths%n", testFiles,
                files.size(), tokens(nameChars));

        Path service = files.stream().filter(p
                -> p.endsWith("AgentService.java")).findFirst()
                .orElseThrow();
        String code = Files.readString(service).replace("\r\n", "\n");
        System.out.println("Edit needs its old_string to appear exactly once in "
                + "AgentService:");
        for (String target : List.of("ledger.finish(",
                "ledger.finish(call.id(), \"OK\", output);",
                "return new Result(", "case \"refusal\" -> {\n"
                        + "                    return new Result(")) {
            int n = occurrences(code, target);
            System.out.printf("  %d match(es) %-6s %s%n", n, n == 1 ? "OK" : "FAILS",
                    target.replace("\n", "\\n").replaceAll(" +", " "));
        }
    }
}
