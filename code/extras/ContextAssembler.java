import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/** Builds one model request from stored context, within a token budget. */
public class ContextAssembler {

    record Turn(String role, String text) {}

    record Stored(String system, String memory, String summary, List<Turn> turns) {}

    record Prompt(String system, List<Turn> messages, int kept, int dropped, int tokens) {}

    /** A rough estimate for this demo. Use the token counting API in production. */
    static int tokens(String text) {
        return Math.max(1, text.length() / 4);
    }

    static Prompt build(Stored stored, String newMessage, int budget) {
        String system = stored.system() + "\n\nKnown about this user: " + stored.memory()
                + "\n\nEarlier in this chat: " + stored.summary();
        int used = tokens(system) + tokens(newMessage);
        if (used > budget) {
            throw new IllegalArgumentException("Fixed context exceeds the budget: " + used);
        }

        // Walk newest to oldest, so that the most recent turns survive.
        Deque<Turn> recent = new ArrayDeque<>();
        List<Turn> turns = stored.turns();
        int index = turns.size() - 1;
        while (index >= 0) {
            int cost = tokens(turns.get(index).text());
            if (used + cost > budget) {
                break;
            }
            recent.addFirst(turns.get(index));
            used += cost;
            index--;
        }

        List<Turn> messages = new ArrayList<>(recent);
        messages.add(new Turn("user", newMessage));
        return new Prompt(system, messages, recent.size(), index + 1, used);
    }

    public static void main(String[] args) {
        List<Turn> turns = new ArrayList<>();
        for (int i = 1; i <= 40; i++) {
            String role = i % 2 == 1 ? "user" : "assistant";
            turns.add(new Turn(role, "Turn " + i + " about refund case 4412. ".repeat(10)));
        }
        Stored stored = new Stored("You are a support assistant.", "Prefers short answers.",
                "The customer wants a refund for order 4412.", turns);

        for (int budget : new int[] {4000, 1000, 300}) {
            Prompt p = build(stored, "Where is my refund?", budget);
            System.out.printf("budget=%d used=%d kept=%d dropped=%d last=%s%n", budget,
                    p.tokens(), p.kept(), p.dropped(), p.messages().getLast().text());
        }
        try {
            build(stored, "Where is my refund?", 20);
        } catch (IllegalArgumentException e) {
            System.out.println("budget=20 rejected: " + e.getMessage());
        }
    }
}
