package course.ai.web;

import course.ai.model.ModelException;
import course.ai.model.ModelPort;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/** One-shot and streaming chat. Identity would come from the token, never from the body. */
@RestController
class ChatController {

    record ChatRequest(@NotBlank @Size(max = 4000) String message) {
    }

    private static final String SYSTEM = "You are a concise support assistant.";
    private static final long STREAM_TIMEOUT_MILLIS = 120_000;

    private final ModelPort model;
    private final ExecutorService streams = Executors.newVirtualThreadPerTaskExecutor();

    ChatController(ModelPort model) {
        this.model = model;
    }

    @PostMapping("/v1/chat")
    Map<String, Object> chat(@Valid @RequestBody ChatRequest request) {
        ModelPort.Turn turn = model.send(new ModelPort.Request(SYSTEM,
                List.of(new ModelPort.UserText(request.message())), List.of()));
        return Map.of("answer", turn.text(), "stopReason", turn.stopReason());
    }

    @PostMapping(path = "/v1/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    SseEmitter stream(@Valid @RequestBody ChatRequest request) {
        SseEmitter emitter = new SseEmitter(STREAM_TIMEOUT_MILLIS);
        AtomicReference<Future<?>> work = new AtomicReference<>();
        // If the user goes away, stop generating: nobody is reading, and tokens cost money.
        emitter.onTimeout(() -> cancel(work));
        emitter.onError(error -> cancel(work));
        work.set(streams.submit(() -> generate(request, emitter)));
        return emitter;
    }

    private void generate(ChatRequest request, SseEmitter emitter) {
        try {
            ModelPort.Turn turn = model.stream(new ModelPort.Request(SYSTEM,
                    List.of(new ModelPort.UserText(request.message())), List.of()),
                    text -> send(emitter, "delta", text));
            send(emitter, "done", turn.stopReason());
            emitter.complete();
        } catch (ModelException e) {
            // Headers have gone already, so the error has to travel as an event.
            send(emitter, "error", e.kind().retryable ? "retry" : "failed");
            emitter.complete();
        } catch (ClientGone e) {
            emitter.complete();
        }
    }

    private static void send(SseEmitter emitter, String event, String data) {
        try {
            emitter.send(SseEmitter.event().name(event).data(data));
        } catch (IOException | IllegalStateException e) {
            throw new ClientGone();
        }
    }

    private static void cancel(AtomicReference<Future<?>> work) {
        Future<?> future = work.get();
        if (future != null) {
            future.cancel(true);
        }
    }

    private static final class ClientGone extends RuntimeException {
    }
}
