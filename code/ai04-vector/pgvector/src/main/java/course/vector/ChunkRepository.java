package course.vector;

import com.pgvector.PGvector;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** AI04 L4: tenant-scoped vector search in Postgres. Compile-checked, not run here. */
public final class ChunkRepository {

    public record Hit(long id, String text, double distance) {
    }

    private static final String SEARCH = """
            SELECT id, text, embedding <=> ? AS distance
            FROM chunks
            WHERE tenant_id = ? AND model_version = ?
            ORDER BY embedding <=> ?
            LIMIT ?""";

    private final Connection connection;

    public ChunkRepository(Connection connection) throws SQLException {
        this.connection = connection;
        PGvector.addVectorType(connection);
    }

    /** SET LOCAL lasts for one transaction, so a pooled connection stays clean. */
    public List<Hit> search(String tenantId, String modelVersion, float[] query, int k)
            throws SQLException {
        boolean previous = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try (Statement settings = connection.createStatement()) {
            settings.execute("SET LOCAL hnsw.ef_search = 100");
            settings.execute("SET LOCAL hnsw.iterative_scan = 'relaxed_order'");
            List<Hit> hits = run(tenantId, modelVersion, query, k);
            connection.commit();
            return hits;
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(previous);
        }
    }

    private List<Hit> run(String tenantId, String modelVersion, float[] query, int k)
            throws SQLException {
        PGvector vector = new PGvector(query);
        try (PreparedStatement statement = connection.prepareStatement(SEARCH)) {
            statement.setObject(1, vector);
            statement.setString(2, tenantId);
            statement.setString(3, modelVersion);
            statement.setObject(4, vector);
            statement.setInt(5, k);
            List<Hit> hits = new ArrayList<>();
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    hits.add(new Hit(
                            rows.getLong(1), rows.getString(2), rows.getDouble(3)));
                }
            }
            return hits;
        }
    }
}
