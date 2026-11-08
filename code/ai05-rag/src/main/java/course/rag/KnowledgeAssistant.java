package course.rag;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * AI05 L7: the decisions around the model call, with fakes in place of every service.
 * Permissions fail closed, and weak retrieval abstains without spending a model call.
 */
public final class KnowledgeAssistant {

    record Chunk(String id, String text, double rerankScore) {
    }

    record Reply(String kind, String detail, boolean modelCalled) {
    }

    interface Directory {
        Optional<Set<String>> groupsOf(String employee);
    }

    interface Search {
        List<Chunk> find(String query, Set<String> groups);
    }

    private static final double MIN_RELEVANCE = 0.40;

    private final Directory directory;
    private final Search search;

    KnowledgeAssistant(Directory directory, Search search) {
        this.directory = directory;
        this.search = search;
    }

    Reply ask(String employee, String question) {
        // Unknown permissions mean no search at all, never an unfiltered one.
        Optional<Set<String>> groups = directory.groupsOf(employee);
        if (groups.isEmpty()) {
            return new Reply("UNAVAILABLE", "permissions could not be checked", false);
        }
        List<Chunk> usable = search.find(question, groups.get()).stream()
                .filter(c -> c.rerankScore() >= MIN_RELEVANCE)
                .toList();
        if (usable.isEmpty()) {
            return new Reply("NOT_FOUND", "no accessible document covers this", false);
        }
        // The real call sends these as documents with citations enabled (CitedAnswers).
        return new Reply("ANSWER", usable.size() + " chunks sent to the model", true);
    }

    public static void main(String[] args) {
        Search search = (query, groups) -> {
            if (!groups.contains("finance")) {
                return List.of(new Chunk("travel-2", "Economy under 6 hours", 0.12));
            }
            return List.of(new Chunk("fin-9", "Quarter close runs for 5 days", 0.91),
                    new Chunk("fin-3", "Accruals are reviewed by controllers", 0.55),
                    new Chunk("travel-2", "Economy on flights under 6 hours", 0.08));
        };
        Directory directory = employee -> switch (employee) {
            case "asha" -> Optional.of(Set.of("finance", "all-staff"));
            case "ravi" -> Optional.of(Set.of("all-staff"));
            default -> Optional.empty();
        };
        KnowledgeAssistant assistant = new KnowledgeAssistant(directory, search);
        String question = "How long is quarter close?";
        for (String employee : List.of("asha", "ravi", "directory-down")) {
            System.out.printf("%-15s %s%n", employee, assistant.ask(employee, question));
        }
    }
}
