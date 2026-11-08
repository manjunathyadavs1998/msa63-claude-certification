package course.frameworks;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.tool.ToolExecutionErrorHandler;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * AI07 L4: real LangChain4j code running against a scripted ChatModel, with no API key.
 * The model's replies are fixed. What's real is everything the framework does around them.
 */
public final class LangChain4jLab {

    static final class ScriptedModel implements ChatModel {
        final List<ChatRequest> seen = new ArrayList<>();
        private final Iterator<AiMessage> script;

        ScriptedModel(List<AiMessage> replies) {
            this.script = replies.iterator();
        }

        @Override
        public ChatResponse doChat(ChatRequest request) {
            seen.add(request);
            return ChatResponse.builder().aiMessage(script.next()).build();
        }
    }

    static class OrderTools {
        int calls;

        @Tool("Look up one order by ID. Use this for a known order number.")
        String lookupOrder(@P("Digits only, such as 7781") String orderId) {
            calls++;
            if (orderId.equals("0000")) {
                throw new IllegalStateException("orders-db password rejected at 10.2.3.4");
            }
            return "Order " + orderId + ": delivered, total $40";
        }
    }

    interface Support {
        @SystemMessage("You are a support agent.")
        String chat(@MemoryId String customerId, @UserMessage String message);
    }

    record Refund(String orderId, double amount) {
    }

    interface RefundExtractor {
        @UserMessage("Extract the refund from: {{it}}")
        Refund extract(String text);
    }

    private LangChain4jLab() {
    }

    static AiMessage toolCall(String orderId) {
        return AiMessage.from(ToolExecutionRequest.builder().id("t1").name("lookupOrder")
                .arguments("{\"orderId\":\"" + orderId + "\"}").build());
    }

    static String roles(ChatRequest request) {
        StringBuilder out = new StringBuilder();
        for (ChatMessage message : request.messages()) {
            out.append(message.type()).append(' ');
        }
        return out.toString().trim();
    }

    public static void main(String[] args) {
        System.out.println("1. An interface becomes a service, with memory per customer");
        OrderTools tools = new OrderTools();
        ScriptedModel model = new ScriptedModel(List.of(toolCall("7781"),
                AiMessage.from("Order 7781 was delivered."),
                AiMessage.from("You're welcome.")));
        Support support = AiServices.builder(Support.class).chatModel(model).tools(tools)
                .chatMemoryProvider(id -> MessageWindowChatMemory.withMaxMessages(10))
                .build();
        System.out.println("  answer: " + support.chat("c-19", "Where is order 7781?"));
        System.out.println("  answer: " + support.chat("c-19", "Thanks"));
        int round = 1;
        for (ChatRequest request : model.seen) {
            System.out.printf("  request %d: %s%n", round++, roles(request));
        }
        System.out.println("  tool description sent: "
                + model.seen.getFirst().toolSpecifications().getFirst().description());

        System.out.println("2. A tool throws: default, then the recommended handler");
        ScriptedModel failing = new ScriptedModel(List.of(toolCall("0000"),
                AiMessage.from("Sorry.")));
        AiServices.builder(Support.class).chatModel(failing).tools(new OrderTools())
                .chatMemoryProvider(id -> MessageWindowChatMemory.withMaxMessages(10))
                .build().chat("c-19", "Where is order 0000?");
        System.out.println("  model sees: " + failing.seen.get(1).messages().getLast());
        ScriptedModel guarded = new ScriptedModel(List.of(toolCall("0000"),
                AiMessage.from("Sorry.")));
        try {
            AiServices.builder(Support.class).chatModel(guarded).tools(new OrderTools())
                    .chatMemoryProvider(id -> MessageWindowChatMemory.withMaxMessages(10))
                    .toolExecutionErrorHandler(
                            ToolExecutionErrorHandler.failInvocationUnlessVisibleToLlm())
                    .build().chat("c-19", "Where is order 0000?");
        } catch (RuntimeException e) {
            System.out.println("  with handler: the call failed, and the model made "
                    + guarded.seen.size() + " request(s)");
        }

        System.out.println("3. Structured output");
        ScriptedModel typed = new ScriptedModel(List.of(
                AiMessage.from("{\"orderId\":\"7781\",\"amount\":40.0}")));
        Refund refund = AiServices.create(RefundExtractor.class, typed)
                .extract("Please refund $40 on order 7781");
        System.out.println("  parsed: " + refund);
        System.out.println("  sent: " + typed.seen.getFirst().messages().getFirst()
                .toString().replace("\n", " ").substring(0, 150));

        System.out.println("4. A model that never stops calling tools");
        List<AiMessage> forever = new ArrayList<>();
        for (int i = 0; i < 400; i++) {
            forever.add(toolCall("7781"));
        }
        OrderTools counted = new OrderTools();
        try {
            AiServices.builder(Support.class).chatModel(new ScriptedModel(forever))
                    .tools(counted)
                    .chatMemoryProvider(id -> MessageWindowChatMemory.withMaxMessages(10))
                    .build().chat("c-19", "Where is it?");
        } catch (RuntimeException e) {
            System.out.println("  stopped after " + counted.calls + " tool runs: "
                    + e.getMessage());
        }
    }
}
