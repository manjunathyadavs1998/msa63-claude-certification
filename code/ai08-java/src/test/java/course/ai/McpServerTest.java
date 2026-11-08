package course.ai;

import static org.assertj.core.api.Assertions.assertThat;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpSchema;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

/** The official SDK's client talks to our server over real Streamable HTTP on localhost. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class McpServerTest {

    @LocalServerPort
    int port;

    @Test
    void clientDiscoversAndCallsOverHttp() {
        var transport = HttpClientStreamableHttpTransport
                .builder("http://localhost:" + port).endpoint("/mcp").build();
        try (McpSyncClient client = McpClient.sync(transport).build()) {
            McpSchema.InitializeResult init = client.initialize();
            System.out.println("LAB|protocol version: " + init.protocolVersion());
            System.out.println("LAB|server: " + init.serverInfo().name());
            McpSchema.ListToolsResult tools = client.listTools();
            System.out.println("LAB|tools: " + tools.tools().stream()
                    .map(McpSchema.Tool::name).toList());
            System.out.println("LAB|resource: " + client.readResource(
                    new McpSchema.ReadResourceRequest("orders://schema")).contents());
            for (String id : new String[] {"7781", "9999", "abc"}) {
                McpSchema.CallToolResult result =
                        client.callTool(new McpSchema.CallToolRequest(
                        "lookup_order", Map.of("order_id", id)));
                System.out.println("LAB|call " + id + ": isError=" + result.isError() + " "
                        + ((McpSchema.TextContent) result.content().get(0)).text());
            }
            McpSchema.CallToolResult ok = client.callTool(new McpSchema.CallToolRequest(
                    "lookup_order", Map.of("order_id", "7781")));
            assertThat(ok.isError()).isFalse();
            try {
                client.callTool(new McpSchema.CallToolRequest("delete_order", Map.of()));
            } catch (RuntimeException e) {
                System.out.println("LAB|unknown tool: " + e.getClass().getSimpleName()
                        + ": " + e.getMessage());
            }
        }
    }
}
