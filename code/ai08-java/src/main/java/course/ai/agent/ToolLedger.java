package course.ai.agent;

import java.util.Optional;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/** Audit trail and idempotency. The tool_use ID is the key, so retries can't repeat
 * effects. */
@Repository
public class ToolLedger {

    private final JdbcTemplate jdbc;

    public ToolLedger(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** True if this call is new. False means it already started, so it must not run again.
     * */
    public boolean begin(String toolUseId, String caseId, String tool, String input) {
        try {
            jdbc.update("""
                    INSERT INTO tool_calls (tool_use_id, case_id, tool, input, decision)
                    VALUES (?, ?, ?, ?, 'STARTED')""", toolUseId, caseId, tool, input);
            return true;
        } catch (DuplicateKeyException alreadyThere) {
            return false;
        }
    }

    public void finish(String toolUseId, String decision, String result) {
        jdbc.update("UPDATE tool_calls SET decision = ?, result = ? WHERE tool_use_id = ?",
                decision, result, toolUseId);
    }

    public Optional<String> previousResult(String toolUseId) {
        return jdbc.query("SELECT result FROM tool_calls WHERE tool_use_id = ?",
                (row, n) -> row.getString(1), toolUseId).stream()
                .filter(java.util.Objects::nonNull).findFirst();
    }

    public int count(String caseId, String tool, String decision) {
        Integer found = jdbc.queryForObject("""
                SELECT COUNT(*) FROM tool_calls
                WHERE case_id = ? AND tool = ? AND decision = ?""",
                Integer.class, caseId, tool, decision);
        return found == null ? 0 : found;
    }
}
