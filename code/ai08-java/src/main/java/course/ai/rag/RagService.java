package course.ai.rag;

import course.ai.model.ModelPort;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

/**
 * AI05 L7's query path. Permissions are checked before search, and weak evidence abstains
 * with no model call. Storage and embedding sit behind ports.
 */
@Service
public class RagService {

    public record Chunk(String id, String source, String published, String text) {
    }

    public record Hit(Chunk chunk, int rank) {
    }

    public interface Directory {
        Set<String> groupsOf(String employee);
    }

    public interface ChunkSearch {
        List<Hit> byMeaning(String query, Set<String> groups, int limit);

        List<Hit> byKeywords(String query, Set<String> groups, int limit);
    }

    public record Answer(String outcome, String text, List<Chunk> sources) {
    }

    private static final int RRF_K = 60;
    private static final int TOP_K = 4;
    private static final int CANDIDATES = 50;

    private final ModelPort model;
    private final Directory directory;
    private final ChunkSearch search;

    public RagService(ModelPort model, Directory directory, ChunkSearch search) {
        this.model = model;
        this.directory = directory;
        this.search = search;
    }

    public Answer ask(String employee, String question) {
        Set<String> groups;
        try {
            groups = directory.groupsOf(employee);
        } catch (RuntimeException unavailable) {
            return new Answer("UNAVAILABLE", "Permissions could not be checked.",
                    List.of());
        }
        List<Chunk> top = fuse(search.byMeaning(question, groups, CANDIDATES),
                search.byKeywords(question, groups, CANDIDATES));
        if (top.isEmpty()) {
            return new Answer("NOT_FOUND", "No document you can access covers this.",
                    List.of());
        }
        ModelPort.Turn turn = model.send(new ModelPort.Request(SYSTEM,
                List.of(new ModelPort.UserText(prompt(top, question))), List.of()));
        return new Answer("ANSWER", turn.text(), top);
    }

    private static final String SYSTEM = """
            Answer only from the documents. If they don't contain the answer, say so.
            Where documents disagree, report both, with sources and dates.""";

    /** Reciprocal Rank Fusion: only positions matter, so no score scaling is needed. */
    static List<Chunk> fuse(List<Hit> meaning, List<Hit> keywords) {
        Map<String, Double> scores = new HashMap<>();
        Map<String, Chunk> chunks = new HashMap<>();
        for (List<Hit> list : List.of(meaning, keywords)) {
            for (Hit hit : list) {
                scores.merge(hit.chunk().id(), 1.0 / (RRF_K + hit.rank()), Double::sum);
                chunks.put(hit.chunk().id(), hit.chunk());
            }
        }
        return scores.entrySet().stream()
                .sorted(Map.Entry.<String,
                        Double>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(TOP_K)
                .map(entry -> chunks.get(entry.getKey()))
                .toList();
    }

    /** Documents first and the question last. Sources and dates are there to be cited. */
    static String prompt(List<Chunk> chunks, String question) {
        List<String> parts = new ArrayList<>();
        parts.add("<documents>");
        int index = 1;
        for (Chunk chunk : chunks) {
            parts.add("<document index=\"" + index++ + "\"><source>" + chunk.source()
                    + "</source><published>" + chunk.published()
                    + "</published><document_content>" + chunk.text()
                    + "</document_content></document>");
        }
        parts.add("</documents>");
        parts.add("Question: " + question);
        return String.join("\n", parts);
    }
}
