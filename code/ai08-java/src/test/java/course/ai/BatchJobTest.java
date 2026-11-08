package course.ai;

import static org.assertj.core.api.Assertions.assertThat;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import course.ai.async.BatchJob;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** The real SDK against the local fake API. The fake's JSON is written from the reference.
 * */
class BatchJobTest {

    @Test
    void submitsThenMatchesResultsByCustomId() throws Exception {
        try (FakeAnthropicServer server = new FakeAnthropicServer()) {
            AnthropicClient client = AnthropicOkHttpClient.builder().apiKey("test")
                    .baseUrl(server.url()).build();
            String batch = """
                    {"id":"batch_1","type":"message_batch","processing_status":"%s",
                     "request_counts":{"processing":0,"succeeded":2,"errored":1,
                     "canceled":0,
                     "expired":0},"created_at":"2026-11-05T00:00:00Z",
                     "expires_at":"2026-11-06T00:00:00Z","ended_at":null,"archived_at":null,
                     "cancel_initiated_at":null,"results_url":null}""";
            server.reply(200, batch.formatted("in_progress"));
            server.reply(200, batch.formatted("ended"));
            String ok = """
                    {"custom_id":"%s","result":{"type":"succeeded","message":%s}}""";
            String results = ok.formatted("doc-b", FakeAnthropicServer.message("end_turn",
                    "[{\"type\":\"text\",\"text\":\"Summary B\"}]")).replace("\n", "")
                            + "\n"
                    + "{\"custom_id\":\"doc-c\",\"result\":{\"type\":\"errored\",\"error\":"
                    + "{\"type\":\"error\",\"error\":{\"type\":\"overloaded_error\","
                    + "\"message\":\"busy\"}}}}\n"
                    + ok.formatted("doc-a", FakeAnthropicServer.message("end_turn",
                    "[{\"type\":\"text\",\"text\":\"Summary A\"}]")).replace("\n", "")
                            + "\n";
            server.reply(200, results);

            BatchJob job = new BatchJob(client, "claude-opus-5-5");
            String id = job.submit(Map.of("doc-a", "Text A", "doc-b", "Text B", "doc-c",
                    "C"));
            System.out.println("LAB|batch id: " + id + ", path "
                    + server.seen.getFirst().path() + ", requests sent: "
                            + new ObjectMapper()
                    .readTree(server.seen.getFirst().body()).get("requests").size());
            System.out.println("LAB|ended? " + job.ended(id));
            BatchJob.Outcome outcome = job.collect(id);
            System.out.println("LAB|results path: " + server.seen.getLast().path());
            System.out.println("LAB|outcome (arrived b, c, a): " + outcome);
            assertThat(outcome.succeeded()).containsEntry("doc-a", "Summary A");
            assertThat(outcome.toResubmit()).containsExactly("doc-c");
            client.close();
        }
    }
}
