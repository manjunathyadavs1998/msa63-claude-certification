package course.vector;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * AI04 L1: calls the embedding endpoint shown in Anthropic's embeddings guide.
 * Compile-checked, not run here. The model version travels with every vector.
 */
public final class EmbeddingClient {

    public enum InputType { QUERY, DOCUMENT }

    public record Embedding(String modelVersion, float[] vector) {
    }

    private static final URI ENDPOINT = URI.create("https://ai.mongodb.com/v1/embeddings");

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final ObjectMapper json = new ObjectMapper();
    private final String apiKey;
    private final String model;

    public EmbeddingClient(String apiKey, String model) {
        this.apiKey = apiKey;
        this.model = model;
    }

    public List<Embedding> embed(List<String> texts, InputType type)
            throws IOException, InterruptedException {
        String body = json.writeValueAsString(Map.of(
                "input", texts,
                "model", model,
                "input_type", type.name().toLowerCase()));
        HttpRequest request = HttpRequest.newBuilder(ENDPOINT)
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response =
                http.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Embedding call failed: " + response.statusCode());
        }
        List<Embedding> out = new ArrayList<>();
        for (JsonNode item : json.readTree(response.body()).path("data")) {
            JsonNode values = item.path("embedding");
            float[] vector = new float[values.size()];
            for (int i = 0; i < vector.length; i++) {
                vector[i] = (float) values.get(i).asDouble();
            }
            out.add(new Embedding(model, vector));
        }
        return out;
    }

    /** Vectors from different models can't be compared, and nothing else will catch it. */
    public static void requireSameModel(String queryModel, String indexModel) {
        if (!queryModel.equals(indexModel)) {
            throw new IllegalStateException(
                    "Query model " + queryModel + " doesn't match index model "
                            + indexModel);
        }
    }
}
