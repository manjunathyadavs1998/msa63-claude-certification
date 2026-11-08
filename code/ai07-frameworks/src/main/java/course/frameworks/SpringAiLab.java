package course.frameworks;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * AI07 L4: real Spring AI code running against a scripted ChatModel, with no API key.
 * The model's replies are fixed. What's real is everything the framework does around them.
 */
public final class SpringAiLab {

    /** Records each prompt the framework sends, and replies from a fixed script. */
    static final class ScriptedModel implements ChatModel {
        final List<Prompt> seen = new ArrayList<>();
        private final Iterator<AssistantMessage> script;

        ScriptedModel(List<AssistantMessage> replies) {
            this.script = replies.iterator();
        }

        @Override
        public ChatResponse call(Prompt prompt) {
            seen.add(prompt);
            return new ChatResponse(List.of(new Generation(script.next())));
        }

        @Override
        public ChatOptions getOptions() {
            return ToolCallingChatOptions.builder().build();
        }
    }

    static class OrderTools {
        int calls;

        @Tool(description = "Look up one order by ID. Use this for a known order number.")
        String lookupOrder(
                @ToolParam(description = "Digits only, such as 7781") String orderId) {
            calls++;
            if (orderId.equals("0000")) {
                throw new IllegalStateException("orders-db password rejected at 10.2.3.4");
            }
            return "Order " + orderId + ": delivered, total $40";
        }
    }

    record Refund(String orderId, double amount) {
    }

    private SpringAiLab() {
    }

    static AssistantMessage toolCall(String orderId) {
        return AssistantMessage.builder().content("Checking the order.").toolCalls(List.of(
                new AssistantMessage.ToolCall("t1", "function", "lookupOrder",
                        "{\"orderId\":\"" + orderId + "\"}"))).build();
    }

    static AssistantMessage text(String content) {
        return AssistantMessage.builder().content(content).build();
    }

    static void showPrompts(ScriptedModel model) {
        int round = 1;
        for (Prompt prompt : model.seen) {
            StringBuilder roles = new StringBuilder();
            for (Message message : prompt.getInstructions()) {
                roles.append(message.getMessageType()).append(' ');
            }
            System.out.printf("  request %d: %s%n", round++, roles.toString().trim());
        }
    }

    public static void main(String[] args) {
        System.out.println("1. Tool loop, run by the framework");
        OrderTools tools = new OrderTools();
        ScriptedModel model = new ScriptedModel(List.of(toolCall("7781"),
                text("Order 7781 was delivered.")));
        String answer = ChatClient.create(model).prompt()
                .system("You are a support agent.")
                .user("Where is order 7781?")
                .tools(tools)
                .call()
                .content();
        showPrompts(model);
        System.out.println("  tool ran " + tools.calls + " time, answer: " + answer);

        System.out.println("2. A tool throws: what does the model see by default?");
        ScriptedModel failing = new ScriptedModel(List.of(toolCall("0000"),
                text("Sorry, I couldn't check that.")));
        ChatClient.create(failing).prompt().user("Where is order 0000?")
                .tools(new OrderTools()).call().content();
        Message last = failing.seen.get(1).getInstructions().getLast();
        System.out.println("  " + last);

        System.out.println("3. Structured output: what gets added to the prompt?");
        ScriptedModel typed = new ScriptedModel(List.of(
                text("{\"orderId\":\"7781\",\"amount\":40.0}")));
        Refund refund = ChatClient.create(typed).prompt().user("Refund order 7781")
                .call().entity(Refund.class);
        String sent = typed.seen.getFirst().getUserMessage().getText();
        System.out.println("  parsed: " + refund);
        System.out.println("  sent " + sent.length() + " characters for 17 typed");
        System.out.println("  starts: " + sent.substring(0, 75).replace("\n", " "));

        System.out.println("4. A model that never stops calling tools");
        List<AssistantMessage> forever = new ArrayList<>();
        for (int i = 0; i < 400; i++) {
            forever.add(toolCall("7781"));
        }
        ScriptedModel stuck = new ScriptedModel(forever);
        OrderTools counted = new OrderTools();
        String outcome;
        try {
            outcome = "returned: " + ChatClient.create(stuck).prompt().user("Where is it?")
                    .tools(counted).call().content();
        } catch (RuntimeException e) {
            outcome = e.getClass().getSimpleName() + ": " + e.getMessage();
        }
        System.out.println("  " + counted.calls + " tool runs, " + stuck.seen.size()
                + " requests, " + outcome);
    }
}
