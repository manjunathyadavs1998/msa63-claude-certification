package course.ai;

import static org.assertj.core.api.Assertions.assertThat;

import course.ai.async.ReviewWorker;
import course.ai.model.ModelException;
import course.ai.model.ModelPort;
import course.ai.model.ScriptedModel;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;

/** A real embedded Kafka broker in this JVM, with a scripted model behind the worker. */
@SpringBootTest(properties = {
    "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
    "spring.kafka.consumer.auto-offset-reset=earliest",
    "course.kafka.enabled=true"})
@EmbeddedKafka(partitions = 1, topics = {ReviewWorker.TOPIC, ReviewWorker.TOPIC + "-dlt"})
class ReviewWorkerTest {

    static final AtomicInteger CALLS = new AtomicInteger();

    @TestConfiguration
    static class Script {
        @Bean
        @Primary
        ModelPort scripted() {
            return new ScriptedModel(request -> {
                CALLS.incrementAndGet();
                String text = ((ModelPort.UserText) request.history().getFirst()).text();
                if (text.contains("always-busy")) {
                    throw new ModelException(ModelException.Kind.OVERLOADED, "busy", null);
                }
                if (text.contains("bad-request")) {
                    throw new ModelException(ModelException.Kind.BAD_REQUEST, "rejected",
                            null);
                }
                return ScriptedModel.text("Summary of " + text);
            });
        }
    }

    @Autowired
    KafkaTemplate<String, String> kafka;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    EmbeddedKafkaBroker broker;

    private String result(String id) {
        List<String> rows =
                jdbc.query("SELECT result FROM processed_events WHERE event_id = ?",
                (row, n) -> row.getString(1), id);
        return rows.isEmpty() ? null : rows.getFirst();
    }

    private void await(java.util.function.BooleanSupplier done) throws Exception {
        long deadline = System.currentTimeMillis() + 30_000;
        while (!done.getAsBoolean() && System.currentTimeMillis() < deadline) {
            Thread.sleep(100);
        }
    }

    @Test
    void duplicatesPayOnceAndFailuresReachTheDeadLetterTopic() throws Exception {
        Consumer<String, String> dlt = new DefaultKafkaConsumerFactory<>(
                KafkaTestUtils.consumerProps("dlt-check", "true", broker),
                new StringDeserializer(), new StringDeserializer()).createConsumer();
        broker.consumeFromAnEmbeddedTopic(dlt, ReviewWorker.TOPIC + "-dlt");

        kafka.send(ReviewWorker.TOPIC, "e1|first ticket");
        kafka.send(ReviewWorker.TOPIC, "e1|first ticket");
        kafka.send(ReviewWorker.TOPIC, "e2|second ticket");
        await(() -> result("e1") != null && result("e2") != null);
        Thread.sleep(500);
        System.out.println("LAB|3 events, 2 unique: model calls=" + CALLS.get()
                + " e1=" + result("e1"));
        assertThat(CALLS.get()).isEqualTo(2);

        CALLS.set(0);
        kafka.send(ReviewWorker.TOPIC, "e3|always-busy ticket");
        ConsumerRecord<String, String> busy =
                KafkaTestUtils.getSingleRecord(dlt, ReviewWorker.TOPIC + "-dlt",
                        Duration.ofSeconds(30));
        System.out.println("LAB|transient failure: attempts=" + CALLS.get()
                + ", then dead letter: " + busy.value());
        assertThat(CALLS.get()).isEqualTo(3);

        CALLS.set(0);
        kafka.send(ReviewWorker.TOPIC, "e4|bad-request ticket");
        ConsumerRecord<String, String> bad =
                KafkaTestUtils.getSingleRecord(dlt, ReviewWorker.TOPIC + "-dlt",
                        Duration.ofSeconds(30));
        System.out.println("LAB|permanent failure: attempts=" + CALLS.get()
                + ", then dead letter: " + bad.value());
        assertThat(CALLS.get()).isEqualTo(1);
        dlt.close();
    }
}
