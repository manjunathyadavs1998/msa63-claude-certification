package course.claude;

import com.anthropic.client.AnthropicClient;
import com.anthropic.core.http.StreamResponse;
import com.anthropic.models.messages.batches.BatchCreateParams;
import com.anthropic.models.messages.batches.BatchResultsParams;
import com.anthropic.models.messages.batches.MessageBatch;
import com.anthropic.models.messages.batches.MessageBatchIndividualResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** AI03 L5: submit, then correlate results by custom_id. Results arrive in any order. */
public final class BatchJobs {

    private final AnthropicClient client;
    private final String model;

    public BatchJobs(AnthropicClient client, String model) {
        this.client = client;
        this.model = model;
    }

    public String submit(Map<String, String> promptsById) {
        BatchCreateParams.Builder batch = BatchCreateParams.builder();
        promptsById.forEach((id, prompt) -> batch.addRequest(
                BatchCreateParams.Request.builder()
                        .customId(id)
                        .params(BatchCreateParams.Request.Params.builder()
                                .model(model)
                                .maxTokens(4_096L)
                                .addUserMessage(prompt)
                                .build())
                        .build()));
        return client.messages().batches().create(batch.build()).id();
    }

    public boolean isFinished(String batchId) {
        return client.messages().batches().retrieve(batchId).processingStatus()
                .equals(MessageBatch.ProcessingStatus.ENDED);
    }

    /** Returns IDs to resubmit. Only failed items are paid for again. */
    public List<String> collect(String batchId, ResultSink sink) {
        List<String> failedIds = new ArrayList<>();
        BatchResultsParams params = BatchResultsParams.builder()
                .messageBatchId(batchId)
                .build();
        try (StreamResponse<MessageBatchIndividualResponse> results =
                client.messages().batches().resultsStreaming(params)) {
            results.stream().forEach(item -> {
                if (item.result().isSucceeded()) {
                    String message = item.result().asSucceeded().message().toString();
                    sink.accept(item.customId(), message);
                } else {
                    failedIds.add(item.customId());
                }
            });
        }
        return failedIds;
    }

    public interface ResultSink {
        void accept(String customId, String message);
    }
}
