package course.ai.rag;

import course.ai.rag.RagService.Chunk;
import course.ai.rag.RagService.Hit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Postgres and pgvector search, filtered by group inside the query. Compiled, not run:
 * no Postgres is available here. Follows AI04 L4's schema, and needs testing on your
 * version.
 */
public class PgVectorSearch implements RagService.ChunkSearch {

    private final JdbcTemplate jdbc;
    private final Function<String, float[]> embedder;

    public PgVectorSearch(JdbcTemplate jdbc, Function<String, float[]> embedder) {
        this.jdbc = jdbc;
        this.embedder = embedder;
    }

    @Override
    public List<Hit> byMeaning(String query, Set<String> groups, int limit) {
        String vector = java.util.Arrays.toString(embedder.apply(query));
        return hits(jdbc.query("""
                SELECT id, source, published, body FROM chunks
                WHERE allowed_groups && ?::text[]
                ORDER BY embedding <=> ?::vector
                LIMIT ?""", (row, n) -> chunk(row), array(groups), vector, limit));
    }

    @Override
    public List<Hit> byKeywords(String query, Set<String> groups, int limit) {
        return hits(jdbc.query("""
                SELECT id, source, published, body FROM chunks
                WHERE allowed_groups && ?::text[]
                  AND body_tsv @@ websearch_to_tsquery('english', ?)
                ORDER BY ts_rank_cd(body_tsv, websearch_to_tsquery('english', ?)) DESC
                LIMIT ?""", (row, n) -> chunk(row), array(groups), query, query, limit));
    }

    private static Chunk chunk(java.sql.ResultSet row) throws java.sql.SQLException {
        return new Chunk(row.getString(1), row.getString(2), row.getString(3),
                row.getString(4));
    }

    private static String array(Set<String> groups) {
        return "{" + String.join(",", groups) + "}";
    }

    private static List<Hit> hits(List<Chunk> chunks) {
        List<Hit> hits = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            hits.add(new Hit(chunks.get(i), i + 1));
        }
        return hits;
    }
}
