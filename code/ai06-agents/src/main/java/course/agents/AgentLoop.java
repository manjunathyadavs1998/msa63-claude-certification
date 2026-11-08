package course.agents;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * AI06 L2: the agentic loop's control flow against a scripted model, so every stop reason
 * can be tested without an API key. ClaudeAgentLoop shows the same flow on the real SDK.
 */
public final class AgentLoop {

    record ToolCall(String id, String name, String input) {
    }

    record Reply(String stopReason, String text, List<ToolCall> calls) {
    }

    record Outcome(String status, int modelCalls, int toolRuns, String lastText) {
    }

    /** Loop guard: a safety net that reports failure, never the normal way to finish. */
    private static final int SAFETY_LIMIT = 25;

    private AgentLoop() {
    }

    static Outcome run(Iterator<Reply> model, Map<String, Function<String, String>> tools) {
        List<String> history = new ArrayList<>();
        int modelCalls = 0;
        int toolRuns = 0;
        String lastText = "";
        while (modelCalls < SAFETY_LIMIT) {
            Reply reply = model.next();
            modelCalls++;
            lastText = reply.text();
            history.add("assistant: " + reply); // the whole reply, tool_use blocks included
            switch (reply.stopReason()) {
                case "end_turn" -> {
                    return new Outcome("DONE", modelCalls, toolRuns, lastText);
                }
                case "tool_use" -> {
                    // Run every call, then return all results in one user message.
                    List<String> results = new ArrayList<>();
                    for (ToolCall call : reply.calls()) {
                        results.add(call.id() + "=" + execute(tools, call));
                        toolRuns++;
                    }
                    history.add("user: " + results);
                }
                case "pause_turn" -> {
                    // A server tool paused. Resend as is, with no added user message.
                }
                case "max_tokens" -> {
                    // Tool input may be cut off, so never run this turn's tools.
                    return new Outcome("TRUNCATED", modelCalls, toolRuns, lastText);
                }
                case "refusal" -> {
                    return new Outcome("REFUSED", modelCalls, toolRuns, lastText);
                }
                default -> {
                    return new Outcome("UNEXPECTED", modelCalls, toolRuns, lastText);
                }
            }
        }
        return new Outcome("SAFETY_LIMIT", modelCalls, toolRuns, lastText);
    }

    private static String execute(Map<String, Function<String, String>> tools,
            ToolCall call) {
        Function<String, String> tool = tools.get(call.name());
        return tool == null ? "error: unknown tool " + call.name()
                : tool.apply(call.input());
    }

    /** Anti-pattern from TS 1.1: ends when the text sounds finished. */
    static Outcome stopOnWord(Iterator<Reply> model, Map<String, Function<String,
            String>> tools) {
        int modelCalls = 0;
        int toolRuns = 0;
        Reply reply;
        do {
            reply = model.next();
            modelCalls++;
            if (reply.text().toLowerCase().contains("done")) {
                return new Outcome("STOPPED_ON_WORD", modelCalls, toolRuns, reply.text());
            }
            for (ToolCall call : reply.calls()) {
                execute(tools, call);
                toolRuns++;
            }
        } while (!reply.stopReason().equals("end_turn"));
        return new Outcome("DONE", modelCalls, toolRuns, reply.text());
    }

    /** Anti-pattern from TS 1.1: treats a fixed iteration count as the stopping rule. */
    static Outcome stopAtCap(Iterator<Reply> model, Map<String, Function<String,
            String>> tools,
            int cap) {
        int toolRuns = 0;
        String text = "";
        for (int i = 0; i < cap; i++) {
            Reply reply = model.next();
            text = reply.text();
            for (ToolCall call : reply.calls()) {
                execute(tools, call);
                toolRuns++;
            }
        }
        return new Outcome("STOPPED_AT_CAP", cap, toolRuns, text);
    }

    static List<Reply> refundScript() {
        return List.of(
                new Reply("tool_use", "Done reading your message. Checking the order.",
                        List.of(new ToolCall("t1", "lookup_order", "7781"))),
                new Reply("tool_use", "Checking the customer and the refund policy.",
                        List.of(new ToolCall("t2", "get_customer", "c-19"),
                                new ToolCall("t3", "get_policy", "refunds"))),
                new Reply("tool_use", "Eligible. Issuing the refund.",
                        List.of(new ToolCall("t4", "process_refund", "7781"))),
                new Reply("end_turn", "Your refund of $40 is on its way.", List.of()));
    }

    public static void main(String[] args) {
        Map<String, Function<String, String>> tools = Map.of(
                "lookup_order", in -> "order " + in + ": delivered, $40",
                "get_customer", in -> "customer " + in + ": verified",
                "get_policy", in -> "refunds within 30 days",
                "process_refund", in -> "refund issued for " + in);

        System.out.println("Same four-reply script, three ways of stopping:");
        System.out.println(" stop_reason   " + run(refundScript().iterator(), tools));
        System.out.println(" word \"done\"   " + stopOnWord(refundScript().iterator(),
                tools));
        System.out.println(" cap of 2      " + stopAtCap(refundScript().iterator(), tools,
                2));

        System.out.println("Other stop reasons:");
        ToolCall cut = new ToolCall("t9", "process_refund", "77");
        System.out.println(" max_tokens    " + run(List.of(new Reply("max_tokens",
                "Issuing",
                List.of(cut))).iterator(), tools));
        System.out.println(" refusal       " + run(List.of(new Reply("refusal", "",
                List.of())).iterator(), tools));
        System.out.println(" pause_turn    " + run(List.of(new Reply("pause_turn",
                "Searching",
                List.of()), new Reply("end_turn", "Found it.", List.of())).iterator(),
                        tools));
        List<Reply> forever = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            forever.add(new Reply("tool_use", "Trying again",
                    List.of(new ToolCall("t" + i, "lookup_order", "7781"))));
        }
        System.out.println(" stuck model   " + run(forever.iterator(), tools));
    }
}
