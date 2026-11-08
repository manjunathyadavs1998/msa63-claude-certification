package course.agents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * AI06 L3: checks tool definitions before any model sees them. It catches mechanical
 * problems, such as overlap. It can't tell you whether a model will choose well: only an
 * evaluation can do that.
 */
public final class ToolLint {

    record ToolDef(String name, String description, List<String> params) {
    }

    private static final Set<String> STOP_WORDS = Set.of("a", "an", "the", "and", "or",
            "of",
            "to", "for", "this", "use", "it", "in", "by", "with", "from", "not", "when");
    private static final double OVERLAP_LIMIT = 0.5;

    private ToolLint() {
    }

    static List<String> check(List<ToolDef> tools) {
        List<String> problems = new ArrayList<>();
        if (tools.size() > 5) {
            problems.add(tools.size() + " tools on one agent: consider splitting by role");
        }
        for (ToolDef tool : tools) {
            String lower = tool.description().toLowerCase(Locale.ROOT);
            if (lower.split("\\s+").length < 12) {
                problems.add(tool.name() + ": description under 12 words");
            }
            if (!lower.contains("use this") && !lower.contains("use when")) {
                problems.add(tool.name() + ": doesn't say when to use it");
            }
            for (String param : tool.params()) {
                if (Set.of("id", "user", "data", "input", "query2").contains(param)) {
                    problems.add(tool.name() + ": ambiguous parameter '" + param + "'");
                }
            }
        }
        for (int i = 0; i < tools.size(); i++) {
            for (int j = i + 1; j < tools.size(); j++) {
                double score = jaccard(tools.get(i).description(),
                        tools.get(j).description());
                if (score >= OVERLAP_LIMIT) {
                    problems.add(String.format("%s and %s: %.0f%% description overlap",
                            tools.get(i).name(), tools.get(j).name(), score * 100));
                }
            }
        }
        return problems;
    }

    static double jaccard(String a, String b) {
        Set<String> left = words(a);
        Set<String> right = words(b);
        Set<String> both = new HashSet<>(left);
        both.retainAll(right);
        Set<String> either = new HashSet<>(left);
        either.addAll(right);
        return either.isEmpty() ? 0 : (double) both.size() / either.size();
    }

    private static Set<String> words(String text) {
        return Arrays.stream(text.toLowerCase(Locale.ROOT).split("[^a-z0-9_]+"))
                .filter(w -> !w.isEmpty() && !STOP_WORDS.contains(w))
                .collect(Collectors.toSet());
    }

    public static void main(String[] args) {
        List<ToolDef> before = List.of(
                new ToolDef("analyze_content", "Analyzes content and returns the results.",
                        List.of("data")),
                new ToolDef("analyze_document",
                        "Analyzes a document and returns the results.",
                        List.of("id")));
        List<ToolDef> after = List.of(
                new ToolDef("extract_web_results",
                        "Extracts titles, snippets and URLs from one saved search results "
                                + "page. Use this right after web_search. For PDFs, use "
                                + "extract_data_points instead.",
                        List.of("results_page_id")),
                new ToolDef("extract_data_points",
                        "Pulls named figures and dates out of one PDF report, with page "
                                + "numbers. Use this for reports you've downloaded, and "
                                + "never for web pages.",
                        List.of("document_id", "fields")));
        System.out.println("Before:");
        check(before).forEach(p -> System.out.println("  " + p));
        System.out.println("After:");
        List<String> problems = check(after);
        System.out.println("  " + (problems.isEmpty() ? "no problems found" : problems));
    }
}
