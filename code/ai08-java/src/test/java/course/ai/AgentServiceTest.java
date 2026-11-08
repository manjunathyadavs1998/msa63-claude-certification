package course.ai;

import static org.assertj.core.api.Assertions.assertThat;

import course.ai.agent.AgentProperties;
import course.ai.agent.AgentService;
import course.ai.agent.AgentTool;
import course.ai.agent.RefundPolicy;
import course.ai.agent.SupportTools;
import course.ai.agent.ToolLedger;
import course.ai.model.ModelPort;
import course.ai.model.ModelPort.ToolCall;
import course.ai.model.ScriptedModel;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** Real services and a real H2 ledger, with a fixed script standing in for the model. */
@SpringBootTest
class AgentServiceTest {

    @Autowired
    ToolLedger ledger;

    @Autowired
    SupportTools support;

    @Autowired
    List<AgentTool> tools;

    private final AgentProperties limits =
            new AgentProperties(25, 200_000, new BigDecimal("500.00"));
    private AgentTool.Context context;

    @BeforeEach
    void setUp() {
        context = new AgentTool.Context("case-" + UUID.randomUUID(), "c-19");
        support.refundsIssued.clear();
    }

    private static String id() {
        return "toolu_" + UUID.randomUUID();
    }

    private static ToolCall verify() {
        return new ToolCall(id(), "get_customer", Map.of("email", "asha@example.com"));
    }

    private static ToolCall refund(String order, String amount) {
        return new ToolCall(id(), "process_refund", Map.of("order_id", order, "amount",
                amount));
    }

    private AgentService agent(ScriptedModel model) {
        return new AgentService(model, tools, new RefundPolicy(limits), ledger, limits);
    }

    private static ModelPort.ToolResults lastResults(ScriptedModel model) {
        List<ModelPort.Entry> history = model.seen.getLast().history();
        return (ModelPort.ToolResults) history.getLast();
    }

    @Test
    void refundBlockedUntilVerifiedAndOverLimitBlockedAlways() {
        ScriptedModel model = ScriptedModel.of(
                ScriptedModel.tools(refund("7781", "40.00")),
                ScriptedModel.tools(verify()),
                ScriptedModel.tools(refund("7781", "40.00")),
                ScriptedModel.tools(refund("8120", "900.00")),
                ScriptedModel.text("Refunded $40, and escalated the $900."));
        AgentService.Result result = agent(model).run("Support", "Refund me", context);
        for (ModelPort.Request request : model.seen) {
            if (request.history().getLast() instanceof ModelPort.ToolResults r) {
                System.out.println("LAB|agent sees: " + r.results().getFirst().content()
                        + " isError=" + r.results().getFirst().isError());
            }
        }
        System.out.println("LAB|result: " + result);
        System.out.println("LAB|refunds issued: " + support.refundsIssued);
        System.out.println("LAB|ledger denied: "
                + ledger.count(context.caseId(), "process_refund", "DENIED")
                + " ok: " + ledger.count(context.caseId(), "process_refund", "OK"));
        assertThat(support.refundsIssued).containsExactly("7781:40.00");
    }

    @Test
    void aRepeatedToolUseIdDoesNotRepeatTheRefund() {
        ToolCall once = refund("7781", "40.00");
        ScriptedModel model = ScriptedModel.of(
                ScriptedModel.tools(verify()),
                ScriptedModel.tools(once),
                ScriptedModel.tools(once),
                ScriptedModel.text("Done"));
        agent(model).run("Support", "Refund me", context);
        System.out.println("LAB|same tool_use_id twice, refunds issued: "
                + support.refundsIssued.size() + ", second result: "
                + lastResults(model).results().getFirst().content());
        assertThat(support.refundsIssued).hasSize(1);
    }

    /** A crash between begin() and finish(): the refund may have happened. */
    @Test
    void anUnfinishedCallIsNeverRunAgain() {
        ToolCall call = refund("7781", "40.00");
        ledger.begin(call.id(), context.caseId(), call.name(), "{}");
        ScriptedModel model = ScriptedModel.of(ScriptedModel.tools(call),
                ScriptedModel.text("Checking with billing."));
        agent(model).run("Support", "Refund me", context);
        String seen = lastResults(model).results().getFirst().content();
        System.out.println("LAB|crash left a call unfinished: refunds issued="
                + support.refundsIssued.size() + ", model sees: " + seen);
        assertThat(support.refundsIssued).isEmpty();
    }

    @Test
    void aToolCrashNeverLeaksItsMessage() {
        ScriptedModel model = ScriptedModel.of(
                ScriptedModel.tools(new ToolCall(id(), "lookup_order",
                        Map.of("order_id", "5030"))),
                ScriptedModel.text("Sorry"));
        agent(model).run("Support", "Where is it?", context);
        String seen = lastResults(model).results().getFirst().content();
        System.out.println("LAB|tool threw 'orders-db password rejected at 10.2.3.4'");
        System.out.println("LAB|model sees: " + seen);
        assertThat(seen).doesNotContain("10.2.3.4").doesNotContain("password");
    }

    @Test
    void parallelCallsReturnInOrderInOneMessage() {
        ScriptedModel model = ScriptedModel.of(
                ScriptedModel.tools(
                        new ToolCall("t-a" + id(), "lookup_order", Map.of("order_id",
                                "7781")),
                        new ToolCall("t-b" + id(), "lookup_order", Map.of("order_id",
                                "9999")),
                        new ToolCall("t-c" + id(), "lookup_order", Map.of("order_id",
                                "abc"))),
                ScriptedModel.text("Done"));
        agent(model).run("Support", "Check three", context);
        List<String> shown = new ArrayList<>();
        lastResults(model).results().forEach(r -> shown.add(r.toolUseId().substring(0, 3)
                + " "
                + r.isError() + " " + r.content()));
        shown.forEach(line -> System.out.println("LAB|parallel: " + line));
        assertThat(shown).hasSize(3);
        assertThat(shown.get(0)).startsWith("t-a");
        assertThat(shown.get(2)).startsWith("t-c").contains("validation");
    }

    @Test
    void limitsReportFailureRatherThanPretending() {
        ScriptedModel looping = new ScriptedModel(request -> ScriptedModel.tools(
                new ToolCall(id(), "lookup_order", Map.of("order_id", "7781"))));
        AgentService.Result steps = agent(looping).run("Support", "Loop", context);
        System.out.println("LAB|stuck model: " + steps.status() + " after "
                + steps.steps());
        assertThat(steps.status()).isEqualTo(AgentService.Status.STEP_LIMIT);

        ModelPort.Turn huge = new ModelPort.Turn("tool_use", "", List.of(), 150_000, 100,
                0, null);
        AgentService.Result budget = agent(new ScriptedModel(request -> huge))
                .run("Support", "Expensive", context);
        System.out.println("LAB|huge context: " + budget.status() + " after "
                + budget.steps()
                + " steps, tokens=" + budget.tokens());
        assertThat(budget.status()).isEqualTo(AgentService.Status.TOKEN_LIMIT);

        ModelPort.Turn cut = new ModelPort.Turn("max_tokens", "Issuing",
                List.of(refund("7781", "12")), 100, 100, 0, null);
        AgentService.Result truncated = agent(ScriptedModel.of(cut)).run("Support", "Hi",
                context);
        System.out.println("LAB|truncated: " + truncated.status() + ", refunds="
                + support.refundsIssued.size());
        assertThat(support.refundsIssued).isEmpty();
    }
}
