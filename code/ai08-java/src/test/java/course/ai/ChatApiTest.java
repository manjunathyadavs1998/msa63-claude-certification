package course.ai;

import static org.assertj.core.api.Assertions.assertThat;

import course.ai.model.ModelException;
import course.ai.model.ModelPort;
import course.ai.model.ScriptedModel;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/** Real embedded Tomcat, real HTTP and real SSE, with a scripted model behind them. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ChatApiTest {

    static final AtomicReference<RuntimeException> FAIL = new AtomicReference<>();
    static final AtomicInteger WORDS_GENERATED = new AtomicInteger();

    @TestConfiguration
    static class Script {
        @Bean
        @Primary
        ModelPort scripted() {
            return new ModelPort() {
                private final ScriptedModel inner = new ScriptedModel(
                        request -> ScriptedModel.text(text(request)),
                                30);

                @Override
                public Turn send(Request request) {
                    if (FAIL.get() != null) {
                        throw FAIL.get();
                    }
                    return inner.send(request);
                }

                @Override
                public Turn stream(Request request,
                        java.util.function.Consumer<String> onText) {
                    if (FAIL.get() != null) {
                        throw FAIL.get();
                    }
                    return inner.stream(request, text -> {
                        WORDS_GENERATED.incrementAndGet();
                        onText.accept(text);
                    });
                }
            };
        }
    }

    private static String text(ModelPort.Request request) {
        String asked = ((ModelPort.UserText) request.history().getFirst()).text();
        return asked.equals("long") ? "word ".repeat(200)
                : "Your order 7781 was delivered.";
    }

    @LocalServerPort
    int port;

    @Autowired
    ModelPort model;

    private final HttpClient http = HttpClient.newHttpClient();

    private HttpRequest post(String path, String body) {
        return HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
    }

    @Test
    void streamsEventsOverRealHttp() throws Exception {
        FAIL.set(null);
        HttpResponse<java.io.InputStream> response = http.send(
                post("/v1/chat/stream", "{\"message\":\"Where is my order?\"}"),
                HttpResponse.BodyHandlers.ofInputStream());
        long start = System.nanoTime();
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isBlank()) {
                    lines.add(line);
                    if (lines.size() == 2) {
                        System.out.println("LAB|first delta after ms (varies): "
                                + (System.nanoTime() - start) / 1_000_000);
                    }
                }
            }
        }
        System.out.println("LAB|content-type: "
                + response.headers().firstValue("content-type").orElse(""));
        System.out.println("LAB|sse lines: " + lines);
        assertThat(lines).contains("event:done", "data:end_turn");
    }

    @Test
    void generationStopsSoonAfterTheClientLeaves() throws Exception {
        FAIL.set(null);
        WORDS_GENERATED.set(0);
        HttpResponse<java.io.InputStream> response = http.send(
                post("/v1/chat/stream", "{\"message\":\"long\"}"),
                HttpResponse.BodyHandlers.ofInputStream());
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(response.body(), StandardCharsets.UTF_8));
        for (int i = 0; i < 6; i++) {
            reader.readLine();
        }
        reader.close();
        Thread.sleep(2_000);
        int afterLeaving = WORDS_GENERATED.get();
        Thread.sleep(1_000);
        System.out.println("LAB|asked for 200 words, client left early: generated="
                + afterLeaving + ", a second later=" + WORDS_GENERATED.get());
        assertThat(WORDS_GENERATED.get()).isEqualTo(afterLeaving);
        assertThat(afterLeaving).isLessThan(200);
    }

    @Test
    void validationFailureIsA400BeforeAnyModelCall() throws Exception {
        FAIL.set(null);
        HttpResponse<String> response = http.send(post("/v1/chat", "{\"message\":\"\"}"),
                HttpResponse.BodyHandlers.ofString());
        System.out.println("LAB|blank message: " + response.statusCode());
        assertThat(response.statusCode()).isEqualTo(400);
    }

    @Test
    void modelFailuresBecomeSafeProblems() throws Exception {
        FAIL.set(new ModelException(ModelException.Kind.RATE_LIMITED, "secret detail",
                null));
        HttpResponse<String> busy = http.send(post("/v1/chat", "{\"message\":\"Hi\"}"),
                HttpResponse.BodyHandlers.ofString());
        System.out.println("LAB|rate limited: " + busy.statusCode() + " " + busy.body());
        assertThat(busy.statusCode()).isEqualTo(429);
        assertThat(busy.body()).doesNotContain("secret detail");

        FAIL.set(new ModelException(ModelException.Kind.AUTH, "bad key sk-ant-123", null));
        HttpResponse<String> auth = http.send(post("/v1/chat", "{\"message\":\"Hi\"}"),
                HttpResponse.BodyHandlers.ofString());
        System.out.println("LAB|bad key: " + auth.statusCode() + " " + auth.body());
        assertThat(auth.statusCode()).isEqualTo(500);
        assertThat(auth.body()).doesNotContain("sk-ant");
        FAIL.set(null);
    }

    @Test
    void streamFailureArrivesAsAnEvent() throws Exception {
        FAIL.set(new ModelException(ModelException.Kind.OVERLOADED, "busy", null));
        HttpResponse<String> response = http.send(
                post("/v1/chat/stream", "{\"message\":\"Hi\"}"),
                HttpResponse.BodyHandlers.ofString());
        FAIL.set(null);
        System.out.println("LAB|stream failure: status=" + response.statusCode() + " body="
                + response.body().replace("\n", " "));
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("event:error");
    }
}
