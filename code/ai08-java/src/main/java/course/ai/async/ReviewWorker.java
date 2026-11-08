package course.ai.async;

import course.ai.model.ModelException;
import course.ai.model.ModelPort;
import java.util.List;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Slow model work behind Kafka. At-least-once delivery means duplicates happen, so the
 * event
 * ID guards against paying and writing twice. Failed events retry, then go to a dead
 * letter.
 */
@Component
public class ReviewWorker {

    public static final String TOPIC = "reviews";
    private final ModelPort model;
    private final JdbcTemplate jdbc;

    public ReviewWorker(ModelPort model, JdbcTemplate jdbc) {
        this.model = model;
        this.jdbc = jdbc;
    }

    @KafkaListener(topics = TOPIC, groupId = "review-workers", id = "reviewWorker",
            autoStartup = "${course.kafka.enabled:false}")
    public void onEvent(String event) {
        String id = event.substring(0, event.indexOf('|'));
        String text = event.substring(event.indexOf('|') + 1);
        try {
            jdbc.update("INSERT INTO processed_events (event_id) VALUES (?)", id);
        } catch (DuplicateKeyException alreadySeen) {
            return;
        }
        try {
            ModelPort.Turn turn = model.send(new ModelPort.Request("Summarize in one line.",
                    List.of(new ModelPort.UserText(text)), List.of()));
            jdbc.update("UPDATE processed_events SET result = ? WHERE event_id = ?",
                    turn.text(), id);
        } catch (ModelException e) {
            // Release the claim, so the retry can do the work.
            jdbc.update("DELETE FROM processed_events WHERE event_id = ?", id);
            if (!e.kind().retryable) {
                throw new KafkaErrors.PermanentFailure(e);
            }
            throw e;
        }
    }
}
