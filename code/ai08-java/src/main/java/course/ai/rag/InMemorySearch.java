package course.ai.rag;

import course.ai.rag.RagService.Chunk;
import course.ai.rag.RagService.Hit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Stands in for Postgres. "Meaning" is word overlap with a small synonym table, so this
 * tests
 * the service, not retrieval quality. Permission filtering happens before ranking, as in
 * SQL.
 */
@Component
public class InMemorySearch implements RagService.ChunkSearch, RagService.Directory {

    private record Stored(Chunk chunk, Set<String> groups) {
    }

    private static final Map<String, String> SYNONYMS = Map.of("money", "refund", "back",
            "refund");
    private final List<Stored> chunks = new ArrayList<>();
    private final Map<String, Set<String>> people = new java.util.HashMap<>();

    public void add(Chunk chunk, String... groups) {
        chunks.add(new Stored(chunk, Set.of(groups)));
    }

    public void person(String employee, String... groups) {
        people.put(employee, Set.of(groups));
    }

    @Override
    public Set<String> groupsOf(String employee) {
        Set<String> groups = people.get(employee);
        if (groups == null) {
            throw new IllegalStateException("Directory unavailable");
        }
        return groups;
    }

    @Override
    public List<Hit> byMeaning(String query, Set<String> groups, int limit) {
        Set<String> wanted = new HashSet<>();
        words(query).forEach(word -> wanted.add(SYNONYMS.getOrDefault(word, word)));
        return rank(wanted, groups, limit);
    }

    @Override
    public List<Hit> byKeywords(String query, Set<String> groups, int limit) {
        return rank(words(query), groups, limit);
    }

    private List<Hit> rank(Set<String> wanted, Set<String> groups, int limit) {
        List<Chunk> found = chunks.stream()
                .filter(stored -> stored.groups().stream().anyMatch(groups::contains))
                .filter(stored -> overlap(stored.chunk(), wanted) > 0)
                .sorted(Comparator.comparingLong(
                        (Stored stored) -> overlap(stored.chunk(), wanted)).reversed())
                .limit(limit)
                .map(Stored::chunk)
                .toList();
        List<Hit> hits = new ArrayList<>();
        for (int i = 0; i < found.size(); i++) {
            hits.add(new Hit(found.get(i), i + 1));
        }
        return hits;
    }

    private static long overlap(Chunk chunk, Set<String> wanted) {
        return words(chunk.text()).stream().filter(wanted::contains).count();
    }

    private static Set<String> words(String text) {
        Set<String> out = new HashSet<>();
        for (String word : text.toLowerCase(Locale.ROOT).split("[^a-z0-9-]+")) {
            if (word.length() > 3) {
                out.add(word);
            }
        }
        return out;
    }
}
