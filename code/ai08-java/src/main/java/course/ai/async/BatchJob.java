package course.ai.async;

import com.anthropic.client.AnthropicClient;
import com.anthropic.core.http.StreamResponse;
import com.anthropic.models.messages.batches.BatchCreateParams;
import com.anthropic.models.messages.batches.MessageBatch;
import com.anthropic.models.messages.batches.MessageBatchIndividualResponse;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Message Batches: 50% cheaper, up to 24 hours, and results in any order. Match results by
 * custom_id, and resubmit only failures. Runs against the local fake API in tests only.
 */
public class BatchJob {

    public record Outcome(Map<String, String> succeeded, List<String> toResubmit) {
    }

    private final AnthropicClient client;
    private final String model;

    public BatchJob(AnthropicClient client, String model) {
        this.client = client;
        this.model = model;
    }

    public String submit(Map<String, String> documentsById) {
        BatchCreateParams.Builder batch = BatchCreateParams.builder();
        documentsById.forEach((id, text)
                -> batch.addRequest(BatchCreateParams.Request.builder()
                .customId(id)
                .params(BatchCreateParams.Request.Params.builder()
                        .model(model)
                        .maxTokens(1024L)
                        .addUserMessage("Summarize in one line: " + text)
                        .build())
                .build()));
        return client.messages().batches().create(batch.build()).id();
    }

    public boolean ended(String batchId) {
        MessageBatch batch = client.messages().batches().retrieve(batchId);
        return batch.processingStatus().equals(MessageBatch.ProcessingStatus.ENDED);
    }

    public Outcome collect(String batchId) {
        Map<String, String> succeeded = new LinkedHashMap<>();
        List<String> failed = new ArrayList<>();
        try (StreamResponse<MessageBatchIndividualResponse> results =
                client.messages().batches().resultsStreaming(batchId)) {
            results.stream().forEach(item -> {
                if (item.result().isSucceeded()) {
                    StringBuilder text = new StringBuilder();
                    item.result().asSucceeded().message().content().forEach(block ->
                            block.text().ifPresent(t -> text.append(t.text())));
                    succeeded.put(item.customId(), text.toString());
                } else {
                    failed.add(item.customId());
                }
            });
        }
        return new Outcome(succeeded, failed);
    }
}
