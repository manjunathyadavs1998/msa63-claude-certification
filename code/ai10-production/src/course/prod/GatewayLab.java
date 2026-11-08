package course.prod;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI10 L7: a small real HTTP gateway, in front of a local fake model server, on localhost.
 * It relays server-sent events, reserves budget, fails over before the first byte, and
 * meters usage. The event shapes are ones I wrote from the docs, so this can't prove the
 * real API agrees. Timings are from one laptop, and they vary.
 */
public final class GatewayLab {

    private GatewayLab() {
    }

    /** A fake model server: each path behaves differently. */
    static final class FakeUpstream implements AutoCloseable {
        final HttpServer server;
        final AtomicInteger calls = new AtomicInteger();
        final AtomicInteger eventsSent = new AtomicInteger();
        final AtomicInteger cancelled = new AtomicInteger();
        volatile int failWith;
        final int events;
        final long delayMillis;

        FakeUpstream(int events, long delayMillis) throws IOException {
            this.events = events;
            this.delayMillis = delayMillis;
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
            server.createContext("/v1/messages", this::handle);
            server.start();
        }

        String url() {
            return "http://127.0.0.1:" + server.getAddress().getPort() + "/v1/messages";
        }

        private void handle(HttpExchange exchange) throws IOException {
            exchange.getRequestBody().readAllBytes();
            calls.incrementAndGet();
            if (failWith != 0) {
                byte[] body =
                        ("{\"type\":\"error\",\"error\":{\"type\":\"overloaded_error\","
                        + "\"message\":\"Busy\"}}").getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(failWith, body.length);
                exchange.getResponseBody().write(body);
                exchange.close();
                return;
            }
            exchange.getResponseHeaders().set("content-type", "text/event-stream");
            exchange.sendResponseHeaders(200, 0);
            try (OutputStream out = exchange.getResponseBody()) {
                send(out, "message_start",
                        "{\"type\":\"message_start\",\"message\":{\"usage\":"
                        + "{\"input_tokens\":1000,\"output_tokens\":1}}}");
                for (int i = 1; i <= events; i++) {
                    sleep(delayMillis);
                    send(out, "content_block_delta", "{\"type\":\"content_block_delta\","
                            + "\"delta\":{\"type\":\"text_delta\",\"text\":\"t" + i
                                    + "\"}}");
                    eventsSent.incrementAndGet();
                }
                send(out, "message_delta", "{\"type\":\"message_delta\",\"usage\":"
                        + "{\"output_tokens\":" + events + "}}");
                send(out, "message_stop", "{\"type\":\"message_stop\"}");
            } catch (IOException gone) {
                cancelled.incrementAndGet();
            }
        }

        private static void send(OutputStream out, String event, String data)
                throws IOException {
            out.write(("event: " + event + "\ndata: " + data + "\n\n")
                    .getBytes(StandardCharsets.UTF_8));
            out.flush();
        }

        @Override
        public void close() {
            server.stop(0);
        }
    }

    /** Reserve the worst case before calling, and settle to the real cost afterwards. */
    static final class Budget {
        private long limit;
        private long committed;
        private long reserved;

        Budget(long limit) {
            this.limit = limit;
        }

        synchronized boolean reserve(long amount) {
            if (committed + reserved + amount > limit) {
                return false;
            }
            reserved += amount;
            return true;
        }

        synchronized void settle(long reservedAmount, long actual) {
            reserved -= reservedAmount;
            committed += actual;
        }

        synchronized long committed() {
            return committed;
        }

        synchronized long reservedNow() {
            return reserved;
        }
    }

    /** Micro-dollars. Prices are Sonnet 5.5's: $2 input and $10 output per million. */
    static long microDollars(long input, long output) {
        return input * 2 + output * 10;
    }

    private static final Pattern MAX_TOKENS =
            Pattern.compile("\"max_tokens\"\\s*:\\s*(\\d+)");
    private static final Pattern INPUT = Pattern.compile("\"input_tokens\"\\s*:\\s*(\\d+)");
    private static final Pattern OUTPUT =
            Pattern.compile("\"output_tokens\"\\s*:\\s*(\\d+)");

    static final class Gateway implements AutoCloseable {
        final HttpServer server;
        final HttpClient client = HttpClient.newHttpClient();
        final List<String> upstreams;
        final Map<String, String> keys;
        final Map<String, Budget> budgets = new ConcurrentHashMap<>();
        final List<String> ledger = Collections.synchronizedList(new ArrayList<>());
        final boolean buffer;
        final AtomicInteger failovers = new AtomicInteger();

        Gateway(List<String> upstreams, Map<String, String> keys, Map<String, Long> limits,
                boolean buffer) throws IOException {
            this.upstreams = upstreams;
            this.keys = keys;
            this.buffer = buffer;
            limits.forEach((tenant, limit) -> budgets.put(tenant, new Budget(limit)));
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
            server.createContext("/v1/messages", this::handle);
            server.start();
        }

        String url() {
            return "http://127.0.0.1:" + server.getAddress().getPort() + "/v1/messages";
        }

        private void reject(HttpExchange exchange, int status, String type, String message)
                throws IOException {
            byte[] body = ("{\"type\":\"error\",\"error\":{\"type\":\"" + type
                    + "\",\"message\":\"" + message
                            + "\"}}").getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        }

        private void handle(HttpExchange exchange) throws IOException {
            String tenant = keys.get(exchange.getRequestHeaders().getFirst("x-api-key"));
            if (tenant == null) {
                reject(exchange, 401, "authentication_error", "Unknown gateway key");
                return;
            }
            String body = new String(exchange.getRequestBody().readAllBytes(),
                    StandardCharsets.UTF_8);
            Matcher maxTokens = MAX_TOKENS.matcher(body);
            long max = maxTokens.find() ? Long.parseLong(maxTokens.group(1)) : 4096;
            long estimatedInput = body.length() / 4 + 1000;
            long hold = microDollars(estimatedInput, max);
            Budget budget = budgets.get(tenant);
            if (!budget.reserve(hold)) {
                exchange.getResponseHeaders().set("retry-after", "60");
                reject(exchange, 429, "rate_limit_error", "Tenant budget reached");
                return;
            }
            long actual = 0;
            try {
                actual = forward(exchange, body, tenant);
            } finally {
                budget.settle(hold, actual);
                exchange.close();
            }
        }

        /** Fails over only before the first byte: after that, the client has seen output.
         * */
        private long forward(HttpExchange exchange, String body, String tenant)
                throws IOException {
            HttpResponse<InputStream> response = null;
            for (int i = 0; i < upstreams.size(); i++) {
                try {
                    response = client.send(
                            HttpRequest.newBuilder(URI.create(upstreams.get(i)))
                            .header("x-api-key", "real-provider-key")
                            .POST(HttpRequest.BodyPublishers.ofString(body)).build(),
                            HttpResponse.BodyHandlers.ofInputStream());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IOException(e);
                }
                if (response.statusCode() < 500 || i == upstreams.size() - 1) {
                    break;
                }
                response.body().close();
                failovers.incrementAndGet();
            }
            if (response.statusCode() != 200) {
                byte[] error = response.body().readAllBytes();
                exchange.sendResponseHeaders(response.statusCode(), error.length);
                exchange.getResponseBody().write(error);
                return 0;
            }
            exchange.getResponseHeaders().set("content-type", "text/event-stream");
            exchange.sendResponseHeaders(200, 0);
            OutputStream out = exchange.getResponseBody();
            long input = 0;
            long output = 0;
            long deltas = 0;
            boolean disconnected = false;
            StringBuilder held = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("data:")) {
                        Matcher in = INPUT.matcher(line);
                        input = in.find() ? Long.parseLong(in.group(1)) : input;
                        Matcher outTokens = OUTPUT.matcher(line);
                        output = outTokens.find() ? Long.parseLong(outTokens.group(1))
                                : output;
                        deltas += line.contains("content_block_delta") ? 1 : 0;
                    }
                    if (buffer) {
                        held.append(line).append('\n');
                        continue;
                    }
                    try {
                        out.write((line + "\n").getBytes(StandardCharsets.UTF_8));
                        if (line.isEmpty()) {
                            out.flush();
                        }
                    } catch (IOException gone) {
                        disconnected = true;
                        break;
                    }
                }
                if (buffer) {
                    out.write(held.toString().getBytes(StandardCharsets.UTF_8));
                    out.flush();
                }
            }
            // With no final usage event, estimate from what was seen, and say so in the
            // ledger.
            long billedOutput = Math.max(output, deltas);
            long cost = microDollars(input, billedOutput);
            ledger.add(tenant + " input=" + input + " output=" + billedOutput
                    + (disconnected ? " ESTIMATED, client disconnected" : "")
                    + " cost=" + cost);
            return cost;
        }

        @Override
        public void close() {
            server.stop(0);
        }
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static final String BODY =
            "{\"model\":\"claude-sonnet-5-5\",\"max_tokens\":500,"
            + "\"stream\":true,\"messages\":[{\"role\":\"user\",\"content\":\"Hi\"}]}";

    private static HttpRequest post(String url, String key) {
        return HttpRequest.newBuilder(URI.create(url)).header("x-api-key", key)
                .POST(HttpRequest.BodyPublishers.ofString(BODY)).build();
    }

    /** Milliseconds to the first text delta, and to the end of the stream. */
    private static long[] timed(HttpClient client, String url) throws Exception {
        long start = System.nanoTime();
        HttpResponse<InputStream> response =
                client.send(post(url, "key-acme"),
                        HttpResponse.BodyHandlers.ofInputStream());
        long first = -1;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (first < 0 && line.contains("text_delta")) {
                    first = System.nanoTime() - start;
                }
            }
        }
        return new long[] {first / 1_000_000, (System.nanoTime() - start) / 1_000_000};
    }

    private static long median(List<Long> values) {
        List<Long> sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        return sorted.get(sorted.size() / 2);
    }

    public static void main(String[] args) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        Map<String, String> keys = Map.of("key-acme", "acme", "key-globex", "globex");
        long plenty = 1_000_000_000L;

        try (FakeUpstream upstream = new FakeUpstream(20, 10);
                Gateway relay = new Gateway(List.of(upstream.url()), keys,
                        Map.of("acme", plenty), false);
                Gateway buffered = new Gateway(List.of(upstream.url()), keys,
                        Map.of("acme", plenty), true)) {
            timed(client, upstream.url());
            List<Long> directFirst = new ArrayList<>();
            List<Long> directAll = new ArrayList<>();
            List<Long> relayFirst = new ArrayList<>();
            List<Long> relayAll = new ArrayList<>();
            List<Long> bufferFirst = new ArrayList<>();
            for (int i = 0; i < 30; i++) {
                long[] d = timed(client, upstream.url());
                directFirst.add(d[0]);
                directAll.add(d[1]);
                long[] r = timed(client, relay.url());
                relayFirst.add(r[0]);
                relayAll.add(r[1]);
                bufferFirst.add(timed(client, buffered.url())[0]);
            }
            System.out.println("20 events, 10 ms apart, 30 runs, median ms, one laptop:");
            System.out.printf("  direct:         first text %3d, whole stream %3d%n",
                    median(directFirst), median(directAll));
            System.out.printf("  through relay:  first text %3d, whole stream %3d%n",
                    median(relayFirst), median(relayAll));
            System.out.printf("  buffering gateway: first text %3d%n", median(bufferFirst));
            System.out.println("  last ledger line: " + relay.ledger.getLast());
        }

        System.out.println();
        System.out.println("Failover:");
        try (FakeUpstream primary = new FakeUpstream(3, 1);
                FakeUpstream secondary = new FakeUpstream(3, 1);
                Gateway gateway = new Gateway(List.of(primary.url(), secondary.url()), keys,
                        Map.of("acme", plenty), false)) {
            primary.failWith = 529;
            HttpResponse<String> ok = client.send(post(gateway.url(), "key-acme"),
                    HttpResponse.BodyHandlers.ofString());
            System.out.printf("  primary 529: status %d, primary calls %d, secondary "
                    + "calls %d,"
                    + " failovers %d%n", ok.statusCode(), primary.calls.get(),
                    secondary.calls.get(), gateway.failovers.get());
            secondary.failWith = 529;
            HttpResponse<String> bad = client.send(post(gateway.url(), "key-acme"),
                    HttpResponse.BodyHandlers.ofString());
            System.out.printf("  both 529: status %d, body forwarded: %s, held now %d%n",
                    bad.statusCode(), bad.body().contains("overloaded_error"),
                    gateway.budgets.get("acme").reservedNow());
        }

        System.out.println();
        System.out.println("Authentication and budget:");
        try (FakeUpstream upstream = new FakeUpstream(3, 1);
                Gateway gateway = new Gateway(List.of(upstream.url()), keys,
                        Map.of("acme", 15_000L, "globex", plenty), false)) {
            System.out.println("  unknown key: " + client.send(post(gateway.url(), "nope"),
                    HttpResponse.BodyHandlers.ofString()).statusCode()
                    + ", upstream calls " + upstream.calls.get());
            int[] statuses = new int[6];
            for (int i = 0; i < statuses.length; i++) {
                statuses[i] = client.send(post(gateway.url(), "key-acme"),
                        HttpResponse.BodyHandlers.ofString()).statusCode();
            }
            System.out.println("  acme, budget $0.015, six calls: "
                    + java.util.Arrays.toString(statuses) + ", committed micro-dollars "
                    + gateway.budgets.get("acme").committed());
            System.out.println("  globex at the same moment: "
                    + client.send(post(gateway.url(), "key-globex"),
                            HttpResponse.BodyHandlers.ofString()).statusCode());
        }

        System.out.println();
        System.out.println("Client disconnects after the first text event:");
        try (FakeUpstream upstream = new FakeUpstream(200, 5);
                Gateway gateway = new Gateway(List.of(upstream.url()), keys,
                        Map.of("acme", plenty), false)) {
            HttpResponse<InputStream> response = client.send(post(gateway.url(),
                    "key-acme"),
                    HttpResponse.BodyHandlers.ofInputStream());
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null && !line.contains("text_delta")) {
                    continue;
                }
            }
            sleep(1500);
            System.out.println("  events the upstream generated, of 200: "
                    + upstream.eventsSent.get() + ", upstream saw cancel: "
                    + upstream.cancelled.get());
            System.out.println("  ledger: " + gateway.ledger);
        }

        System.out.println();
        System.out.println("The check-then-spend race, 200 concurrent calls, $1 budget:");
        raceNaive();
        raceReserved();
    }

    /** Each call costs $0.05 and takes 20 ms. The limit allows 20 of them. */
    private static void raceNaive() throws Exception {
        AtomicLong spent = new AtomicLong();
        AtomicInteger admitted = new AtomicInteger();
        long limit = 1_000_000;
        long cost = 50_000;
        try (ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 200; i++) {
                pool.submit(() -> {
                    if (spent.get() + cost <= limit) {
                        admitted.incrementAndGet();
                        sleep(20);
                        spent.addAndGet(cost);
                    }
                });
            }
        }
        System.out.printf("  check, call, then add: admitted %3d, spent $%.2f%n",
                admitted.get(),
                spent.get() / 1e6);
    }

    private static void raceReserved() throws Exception {
        Budget budget = new Budget(1_000_000);
        AtomicInteger admitted = new AtomicInteger();
        long cost = 50_000;
        try (ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 200; i++) {
                pool.submit(() -> {
                    if (budget.reserve(cost)) {
                        admitted.incrementAndGet();
                        sleep(20);
                        budget.settle(cost, cost);
                    }
                });
            }
        }
        System.out.printf("  reserve, call, settle: admitted %3d, spent $%.2f%n",
                admitted.get(),
                budget.committed() / 1e6);
    }
}
