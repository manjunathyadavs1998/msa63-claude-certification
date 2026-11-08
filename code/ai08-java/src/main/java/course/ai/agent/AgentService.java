package course.ai.agent;

import course.ai.agent.AgentTool.Context;
import course.ai.agent.AgentTool.ToolFailure;
import course.ai.model.ModelPort;
import course.ai.model.ModelPort.ToolCall;
import course.ai.model.ModelPort.ToolResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** The loop from AI06 L2, with limits, policy, idempotency, and safe tool errors added. */
@Service
public class AgentService {

    public enum Status { DONE, TRUNCATED, REFUSED, STEP_LIMIT, TOKEN_LIMIT, UNEXPECTED }

    public record Result(Status status, String text, int steps, long tokens) {
    }

    private static final Logger LOG = LoggerFactory.getLogger(AgentService.class);
    private static final ObjectMapper JSON = new ObjectMapper();

    private final ModelPort model;
    private final Map<String, AgentTool> tools;
    private final RefundPolicy policy;
    private final ToolLedger ledger;
    private final AgentProperties limits;

    public AgentService(ModelPort model, List<AgentTool> tools, RefundPolicy policy,
            ToolLedger ledger, AgentProperties limits) {
        this.model = model;
        this.tools = tools.stream().collect(
                Collectors.toMap(t -> t.definition().name(), Function.identity()));
        this.policy = policy;
        this.ledger = ledger;
        this.limits = limits;
    }

    public Result run(String system, String message, Context context) {
        List<ModelPort.Entry> history = new ArrayList<>();
        history.add(new ModelPort.UserText(message));
        List<ModelPort.ToolDef> definitions = tools.values().stream()
                .map(AgentTool::definition)
                // A stable order keeps the prompt cache valid.
                .sorted(java.util.Comparator.comparing(ModelPort.ToolDef::name))
                .toList();
        long tokens = 0;
        for (int step = 1; step <= limits.maxSteps(); step++) {
            ModelPort.Turn turn = model.send(new ModelPort.Request(system,
                    List.copyOf(history), definitions));
            tokens += turn.inputTokens() + turn.outputTokens();
            if (tokens > limits.maxTokensPerRun()) {
                return new Result(Status.TOKEN_LIMIT, turn.text(), step, tokens);
            }
            switch (turn.stopReason()) {
                case "end_turn" -> {
                    return new Result(Status.DONE, turn.text(), step, tokens);
                }
                case "tool_use" -> {
                    history.add(new ModelPort.Assistant(turn));
                    history.add(new ModelPort.ToolResults(runAll(turn.calls(), context)));
                }
                case "pause_turn" -> history.add(new ModelPort.Assistant(turn));
                case "max_tokens" -> {
                    return new Result(Status.TRUNCATED, turn.text(), step, tokens);
                }
                case "refusal" -> {
                    return new Result(Status.REFUSED, turn.text(), step, tokens);
                }
                default -> {
                    return new Result(Status.UNEXPECTED, turn.text(), step, tokens);
                }
            }
        }
        return new Result(Status.STEP_LIMIT, "", limits.maxSteps(), tokens);
    }

    /** Runs a turn's calls together, keeping results in order, and all in one message. */
    private List<ToolResult> runAll(List<ToolCall> calls, Context context) {
        try (ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<ToolResult>> running = new ArrayList<>();
            for (ToolCall call : calls) {
                running.add(pool.submit(() -> runOne(call, context)));
            }
            List<ToolResult> results = new ArrayList<>();
            for (int i = 0; i < running.size(); i++) {
                results.add(join(running.get(i), calls.get(i)));
            }
            return results;
        }
    }

    private static ToolResult join(Future<ToolResult> future, ToolCall call) {
        try {
            return future.get();
        } catch (Exception e) {
            LOG.error("Tool {} crashed", call.name(), e);
            return failure(call, AgentTool.Category.TRANSIENT, true, "The tool failed.");
        }
    }

    private ToolResult runOne(ToolCall call,
            Context context) throws JsonProcessingException {
        String input = JSON.writeValueAsString(call.input());
        if (!ledger.begin(call.id(), context.caseId(), call.name(), input)) {
            String earlier =
                    ledger.previousResult(call.id()).orElse("Already in progress.");
            return new ToolResult(call.id(), earlier, false);
        }
        if (policy.before(call, context) instanceof RefundPolicy.Deny deny) {
            ledger.finish(call.id(), "DENIED", deny.reason());
            return failure(call, AgentTool.Category.BUSINESS, false, deny.reason());
        }
        AgentTool tool = tools.get(call.name());
        if (tool == null) {
            ledger.finish(call.id(), "UNKNOWN", "");
            return failure(call, AgentTool.Category.VALIDATION, false,
                    "There's no tool called " + call.name() + ".");
        }
        try {
            String output = tool.run(call.input(), context);
            policy.after(call, output, context);
            ledger.finish(call.id(), "OK", output);
            return new ToolResult(call.id(), output, false);
        } catch (ToolFailure e) {
            ledger.finish(call.id(), "FAILED", e.getMessage());
            return failure(call, e.category, e.retryable, e.getMessage());
        } catch (RuntimeException e) {
            // The detail stays in our logs. It could hold hosts, SQL or personal data.
            LOG.error("Tool {} threw", call.name(), e);
            ledger.finish(call.id(), "CRASHED", "");
            return failure(call, AgentTool.Category.TRANSIENT, true, "The tool failed.");
        }
    }

    private static ToolResult failure(ToolCall call, AgentTool.Category category,
            boolean retryable, String message) {
        return new ToolResult(call.id(), String.format(
                "{\"errorCategory\":\"%s\",\"isRetryable\":%s,\"message\":\"%s\"}",
                category.name().toLowerCase(), retryable, message.replace("\"", "'")),
                        true);
    }
}
