package course.security;

import java.util.ArrayList;
import java.util.List;

/**
 * AI09 L1: reviews an agent's tools for the "lethal trifecta" named by Simon Willison.
 * It reads declared properties, so it's only as good as those declarations.
 */
public final class TrifectaCheck {

    record Tool(String name, boolean readsPrivateData, boolean readsUntrustedContent,
            boolean canSendOut, boolean changesState) {
    }

    record Agent(String name, List<Tool> tools) {
    }

    private TrifectaCheck() {
    }

    static String review(Agent agent) {
        boolean secret = agent.tools().stream().anyMatch(Tool::readsPrivateData);
        boolean untrusted = agent.tools().stream().anyMatch(Tool::readsUntrustedContent);
        boolean exit = agent.tools().stream().anyMatch(Tool::canSendOut);
        boolean writes = agent.tools().stream().anyMatch(Tool::changesState);
        List<String> legs = new ArrayList<>();
        if (secret) {
            legs.add("private data");
        }
        if (untrusted) {
            legs.add("untrusted content");
        }
        if (exit) {
            legs.add("way out");
        }
        String verdict;
        if (secret && untrusted && exit) {
            verdict = "HIGH: an injection can steal data";
        } else if (untrusted && writes) {
            verdict = "HIGH: an injection can change state";
        } else if (untrusted) {
            verdict = "MEDIUM: an injection can mislead";
        } else {
            verdict = "LOWER: no attacker-controlled input declared";
        }
        return String.format("%-22s %-44s %s", agent.name(), legs, verdict);
    }

    public static void main(String[] args) {
        Tool crm = new Tool("get_customer", true, false, false, false);
        Tool web = new Tool("fetch_url", false, true, true, false);
        Tool email = new Tool("read_inbox", true, true, false, false);
        Tool send = new Tool("send_email", false, false, true, true);
        Tool docs = new Tool("search_wiki", false, false, false, false);
        Tool refund = new Tool("process_refund", false, false, false, true);
        Tool tickets = new Tool("read_ticket", true, true, false, false);

        List<Agent> agents = List.of(
                new Agent("email assistant", List.of(email, send)),
                new Agent("research with CRM", List.of(crm, web)),
                new Agent("research only", List.of(web)),
                new Agent("support with refunds", List.of(tickets, refund)),
                new Agent("support, read-only", List.of(tickets)),
                new Agent("internal wiki helper", List.of(docs)));
        agents.forEach(agent -> System.out.println(review(agent)));
    }
}
