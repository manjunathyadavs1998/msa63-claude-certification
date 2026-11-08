package course.ai.model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/** A test and offline double. It says nothing about model quality, only about our code. */
public class ScriptedModel implements ModelPort {

    private final Function<Request, Turn> script;
    private final long millisPerFragment;
    public final List<Request> seen = new java.util.concurrent.CopyOnWriteArrayList<>();

    public ScriptedModel(Function<Request, Turn> script) {
        this(script, 0);
    }

    public ScriptedModel(Function<Request, Turn> script, long millisPerFragment) {
        this.script = script;
        this.millisPerFragment = millisPerFragment;
    }

    /** Replies in order, ignoring the request. */
    public static ScriptedModel of(Turn... turns) {
        List<Turn> queue = new ArrayList<>(List.of(turns));
        return new ScriptedModel(request -> queue.remove(0));
    }

    public static Turn text(String text) {
        return new Turn("end_turn", text, List.of(), 100, 20, 0, null);
    }

    public static Turn tools(ToolCall... calls) {
        return new Turn("tool_use", "", List.of(calls), 100, 20, 0, null);
    }

    @Override
    public Turn send(Request request) {
        seen.add(request);
        return script.apply(request);
    }

    @Override
    public Turn stream(Request request, Consumer<String> onText) {
        Turn turn = send(request);
        for (String word : turn.text().split("(?<= )")) {
            pause();
            onText.accept(word);
        }
        return turn;
    }

    private void pause() {
        if (millisPerFragment > 0) {
            try {
                Thread.sleep(millisPerFragment);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Stream cancelled", e);
            }
        }
    }
}
