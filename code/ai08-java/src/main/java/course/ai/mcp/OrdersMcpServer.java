package course.ai.mcp;

import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport
        .HttpServletStreamableServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import java.util.List;
import java.util.Map;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** A real MCP server on the official Java SDK, served over Streamable HTTP at /mcp. */
@Configuration
public class OrdersMcpServer {

    private static final Map<String, String> ORDERS = Map.of("7781",
            "delivered, total $40");

    private static McpSchema.CallToolResult result(String text, boolean isError) {
        return McpSchema.CallToolResult.builder()
                .content(List.of(new McpSchema.TextContent(text)))
                .isError(isError)
                .build();
    }

    @Bean
    HttpServletStreamableServerTransportProvider mcpTransport() {
        return HttpServletStreamableServerTransportProvider.builder()
                .jsonMapper(McpJsonDefaults.getMapper())
                .mcpEndpoint("/mcp")
                .build();
    }

    @Bean
    ServletRegistrationBean<?> mcpServlet(
            HttpServletStreamableServerTransportProvider transport) {
        return new ServletRegistrationBean<>(transport, "/mcp");
    }

    @Bean(destroyMethod = "close")
    McpSyncServer ordersServer(HttpServletStreamableServerTransportProvider transport) {
        McpSchema.JsonSchema schema = new McpSchema.JsonSchema("object",
                Map.of("order_id", Map.of("type", "string",
                        "description", "Digits only, such as 7781")),
                List.of("order_id"), false, null, null);
        McpSyncServer server = McpServer.sync(transport)
                .serverInfo("orders", "1.0.0")
                .capabilities(McpSchema.ServerCapabilities.builder()
                        .tools(false)
                        .resources(false, false)
                        .build())
                .build();

        server.addTool(McpServerFeatures.SyncToolSpecification.builder()
                .tool(McpSchema.Tool.builder()
                        .name("lookup_order")
                        .description("""
                                Looks up one order by ID. Use this for a known order \
                                number. \
                                Returns a no-match message, not an error, if none \
                                exists.""")
                        .inputSchema(schema)
                        .build())
                .callHandler((exchange, request) -> {
                    String id = String.valueOf(request.arguments().get("order_id"));
                    if (!id.matches("\\d+")) {
                        return result("{\"errorCategory\":\"validation\","
                                + "\"isRetryable\":false,"
                                + "\"message\":\"order_id must be digits only\"}", true);
                    }
                    String found = ORDERS.get(id);
                    return result(found == null ? "No order " + id + " exists"
                            : "Order " + id + ": " + found, false);
                })
                .build());

        server.addResource(new McpServerFeatures.SyncResourceSpecification(
                McpSchema.Resource.builder()
                        .uri("orders://schema")
                        .name("Order fields")
                        .mimeType("text/plain")
                        .build(),
                (exchange, request) -> new McpSchema.ReadResourceResult(List.of(
                        new McpSchema.TextResourceContents(request.uri(), "text/plain",
                                "order_id, status, total, delivered_on")))));
        return server;
    }
}
