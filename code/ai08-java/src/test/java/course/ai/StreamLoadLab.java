package course.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * AI08 L2: holding many slow "model calls" open. Each task only sleeps, so this measures
 * how
 * threads wait, on this one laptop. It isn't a benchmark of a server or of the Claude API.
 */
public final class StreamLoadLab {

    private static final int MILLIS_WAITING = 2_000;

    private StreamLoadLab() {
    }

    private static void run(String name, ExecutorService pool, int tasks) throws Exception {
        AtomicInteger active = new AtomicInteger();
        AtomicInteger peak = new AtomicInteger();
        long start = System.nanoTime();
        try (pool) {
            List<Future<?>> running = new ArrayList<>();
            for (int i = 0; i < tasks; i++) {
                running.add(pool.submit(() -> {
                    peak.accumulateAndGet(active.incrementAndGet(), Math::max);
                    try {
                        Thread.sleep(MILLIS_WAITING);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    active.decrementAndGet();
                }));
            }
            for (Future<?> future : running) {
                future.get();
            }
        }
        System.out.printf("%-26s tasks=%-6d peak concurrent=%-6d total=%.1fs%n", name,
                tasks,
                peak.get(), (System.nanoTime() - start) / 1e9);
    }

    public static void main(String[] args) throws Exception {
        run("platform pool of 200", Executors.newFixedThreadPool(200), 2_000);
        run("virtual threads", Executors.newVirtualThreadPerTaskExecutor(), 2_000);
        run("virtual threads", Executors.newVirtualThreadPerTaskExecutor(), 20_000);
    }
}
