package course.agents;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.Map;

/**
 * AI06 L4: the JSON-RPC message shapes from MCP protocol version 2026-07-28, handled
 * in-process with no transport. A teaching sketch and not a conforming server. Build real
 * servers with an MCP SDK (AI08 L4).
 */
public final class McpMiniServer {

    private static final String VERSION = "2026-07-28";
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Map<String, String> ORDERS = Map.of("7781",
            "delivered, total $40");

    private McpMiniServer() {
    }

    static String handle(String request) throws Exception {
        JsonNode message = JSON.readTree(request);
        JsonNode id = message.get("id");
        JsonNode params = message.path("params");
        String version = params.path("_meta")
                .path("io.modelcontextprotocol/protocolVersion").asText("");
        if (!VERSION.equals(version)) {
            return error(id, -32602, "Unsupported protocol version: " + version);
        }
        return switch (message.path("method").asText()) {
            case "server/discover" -> ok(id, Map.of("supportedVersions", List.of(VERSION),
                    "capabilities", Map.of("tools", Map.of(), "resources", Map.of())));
            case "tools/list" -> ok(id, Map.of("tools", List.of(Map.of(
                    "name", "lookup_order",
                    "description", "Look up one order by ID. Use this for a known order.",
                    "inputSchema", Map.of("type", "object", "required", List.of("order_id"),
                            "properties", Map.of("order_id", Map.of("type", "string")))))));
            case "tools/call" -> call(id, params);
            case "resources/list" -> ok(id, Map.of("resources", List.of(Map.of(
                    "uri", "orders://schema", "name", "Order fields",
                    "mimeType", "text/plain"))));
            case "resources/read" -> ok(id, Map.of("contents", List.of(Map.of(
                    "uri", params.path("uri").asText(),
                    "text", "order_id, status, total, delivered_on"))));
            default -> error(id, -32601, "Method not found");
        };
    }

    private static String call(JsonNode id, JsonNode params) throws Exception {
        String tool = params.path("name").asText();
        if (!tool.equals("lookup_order")) {
            return error(id, -32602, "Unknown tool: " + tool); // protocol error
        }
        String orderId = params.path("arguments").path("order_id").asText();
        if (!orderId.matches("\\d+")) {
            return result(id, "order_id must be digits only, such as 7781", true);
        }
        String found = ORDERS.get(orderId);
        return found == null
                ? result(id, "No order " + orderId + " exists", false) // valid empty answer
                : result(id, "Order " + orderId + ": " + found, false);
    }

    private static String result(JsonNode id, String text,
            boolean isError) throws Exception {
        return ok(id, Map.of("content", List.of(Map.of("type", "text", "text", text)),
                "isError", isError));
    }

    private static String ok(JsonNode id, Map<String, Object> fields) throws Exception {
        ObjectNode out = JSON.createObjectNode().put("jsonrpc", "2.0");
        out.set("id", id);
        ObjectNode result = JSON.valueToTree(fields);
        result.put("resultType", "complete");
        out.set("result", result);
        return JSON.writeValueAsString(out);
    }

    private static String error(JsonNode id, int code, String text) throws Exception {
        ObjectNode out = JSON.createObjectNode().put("jsonrpc", "2.0");
        out.set("id", id);
        out.set("error", JSON.createObjectNode().put("code", code).put("message", text));
        return JSON.writeValueAsString(out);
    }

    private static String request(int id, String method, String params) {
        String meta = "\"_meta\":{\"io.modelcontextprotocol/protocolVersion\":\"" + VERSION
                + "\"}";
        return "{\"jsonrpc\":\"2.0\",\"id\":" + id + ",\"method\":\"" + method
                + "\",\"params\":{" + meta + params + "}}";
    }

    public static void main(String[] args) throws Exception {
        String call = ",\"name\":\"lookup_order\",\"arguments\":{\"order_id\":\"%s\"}";
        String[][] cases = {
            {"server/discover", ""},
            {"resources/read", ",\"uri\":\"orders://schema\""},
            {"tools/call", String.format(call, "7781")},
            {"tools/call", String.format(call, "9999")},
            {"tools/call", String.format(call, "abc")},
            {"tools/call", ",\"name\":\"delete_order\""},
        };
        int id = 1;
        for (String[] item : cases) {
            String reply = handle(request(id, item[0], item[1]));
            JsonNode node = JSON.readTree(reply);
            System.out.println(item[0] + item[1].replace("\"", "'"));
            System.out.println("  -> " + (node.has("error") ? "error " + node.get("error")
                    : node.get("result")));
            id++;
        }
        String old = "{\"jsonrpc\":\"2.0\",\"id\":9,"
                + "\"method\":\"tools/list\",\"params\":{}}";
        System.out.println("no version -> " + JSON.readTree(handle(old)).get("error"));
    }
}
