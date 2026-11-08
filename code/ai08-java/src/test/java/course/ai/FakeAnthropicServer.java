package course.ai;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;

/**
 * A local stand-in for the Claude API. The SDK is real, and so is its HTTP and JSON. The
 * replies are shapes I wrote from the API reference, so this can't prove the real API
 * agrees.
 */
public final class FakeAnthropicServer implements AutoCloseable {

    public record Canned(int status, String contentType, String body) {
    }

    public record Seen(String path, Map<String, List<String>> headers, String body) {
    }

    private final HttpServer server;
    private final Deque<Canned> replies = new ArrayDeque<>();
    public final List<Seen> seen = new ArrayList<>();

    public FakeAnthropicServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            String body = new String(exchange.getRequestBody().readAllBytes(),
                    StandardCharsets.UTF_8);
            Canned reply;
            synchronized (this) {
                seen.add(new Seen(exchange.getRequestURI().getPath(),
                        exchange.getRequestHeaders(), body));
                reply = replies.isEmpty() ? new Canned(500, "text/plain", "no reply left")
                        : replies.poll();
            }
            byte[] bytes = reply.body().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", reply.contentType());
            exchange.sendResponseHeaders(reply.status(), bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        server.start();
    }

    public synchronized FakeAnthropicServer reply(int status, String body) {
        replies.add(new Canned(status, "application/json", body));
        return this;
    }

    public synchronized FakeAnthropicServer replyStream(String body) {
        replies.add(new Canned(200, "text/event-stream", body));
        return this;
    }

    public synchronized void forget() {
        replies.clear();
        seen.clear();
    }

    public String url() {
        return "http://127.0.0.1:" + server.getAddress().getPort();
    }

    @Override
    public void close() {
        server.stop(0);
    }

    public static String message(String stopReason, String contentJson) {
        return """
                {"id":"msg_1","type":"message","role":"assistant","model":"claude-opus-5-5",
                 "content":%s,"stop_reason":"%s","stop_sequence":null,
                 "usage":{"input_tokens":120,"output_tokens":30,
                          "cache_creation_input_tokens":0,"cache_read_input_tokens":90}}"""
                .formatted(contentJson, stopReason);
    }

    public static String error(String type, String text) {
        return """
                {"type":"error","error":{"type":"%s","message":"%s"},
                "request_id":"req_1"}"""
                .formatted(type, text);
    }

    public static List<String> sseTextEvents(String... fragments) {
        List<String> events = new ArrayList<>();
        events.add(event("message_start", """
                {"type":"message_start","message":{"id":"msg_2","type":"message",
                "role":"assistant","model":"claude-opus-5-5","content":[],
                "stop_reason":null,
                "stop_sequence":null,"usage":{"input_tokens":50,"output_tokens":1}}}"""));
        events.add(event("content_block_start", """
                {"type":"content_block_start","index":0,
                "content_block":{"type":"text","text":""}}"""));
        for (String fragment : fragments) {
            events.add(event("content_block_delta", """
                    {"type":"content_block_delta","index":0,
                    "delta":{"type":"text_delta","text":"%s"}}""".formatted(fragment)));
        }
        events.add(event("content_block_stop", """
                {"type":"content_block_stop","index":0}"""));
        events.add(event("message_delta", """
                {"type":"message_delta","delta":{"stop_reason":"end_turn",
                "stop_sequence":null},
                "usage":{"output_tokens":12}}"""));
        events.add(event("message_stop", """
                {"type":"message_stop"}"""));
        return events;
    }

    private static String event(String name, String json) {
        return "event: " + name + "\ndata: " + json.replace("\n", "") + "\n\n";
    }
}
