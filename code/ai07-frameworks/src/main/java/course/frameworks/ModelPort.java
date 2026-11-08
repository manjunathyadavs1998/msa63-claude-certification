package course.frameworks;

import java.util.ArrayList;
import java.util.List;

/**
 * AI07 L1: keeping a framework behind your own small interface. Business rules and tests
 * depend on this port, so swapping the library touches one adapter. Scripted, no model.
 */
public interface ModelPort {

    record Request(String system, String user, int maxTokens) {
    }

    record Reply(String text, String stopReason, int inputTokens, int outputTokens) {
    }

    Reply send(Request request);

    /** A test double: this is what lets rules be tested with no key and no network. */
    final class Scripted implements ModelPort {
        private final List<Reply> replies;
        final List<Request> seen = new ArrayList<>();

        Scripted(List<Reply> replies) {
            this.replies = replies;
        }

        @Override
        public Reply send(Request request) {
            seen.add(request);
            return replies.get(seen.size() - 1);
        }
    }

    /** Rules your business owns, and no framework can quietly change. */
    final class TicketSummarizer {
        private static final int BUDGET_TOKENS = 500;
        private final ModelPort model;
        int spent;

        TicketSummarizer(ModelPort model) {
            this.model = model;
        }

        String summarize(String ticket) {
            if (spent >= BUDGET_TOKENS) {
                return "BUDGET_EXCEEDED";
            }
            Reply reply = model.send(new Request("Summarize in one line.", ticket, 200));
            spent += reply.inputTokens() + reply.outputTokens();
            return switch (reply.stopReason()) {
                case "end_turn" -> reply.text();
                case "max_tokens" -> "TRUNCATED";
                default -> "FAILED: " + reply.stopReason();
            };
        }
    }

    static void main(String[] args) {
        Scripted model = new Scripted(List.of(
                new Reply("Customer was charged twice.", "end_turn", 120, 10),
                new Reply("Parcel arrived", "max_tokens", 150, 200),
                new Reply("", "refusal", 90, 0)));
        TicketSummarizer summarizer = new TicketSummarizer(model);
        for (String ticket : List.of("t1", "t2", "t3", "t4")) {
            System.out.printf("%s -> %-28s spent=%d%n", ticket,
                    summarizer.summarize(ticket), summarizer.spent);
        }
        System.out.println("Model requests made: " + model.seen.size());
    }
}
