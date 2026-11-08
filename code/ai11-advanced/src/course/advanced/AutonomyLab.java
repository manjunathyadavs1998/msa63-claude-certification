package course.advanced;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * AI11 L4: runs real child processes under time and output limits, and shows what simply
 * killing the parent leaves behind. This is containment of resources only. It's no sandbox:
 * these children can still read files and reach the network. Results are from Windows.
 */
public final class AutonomyLab {

    private AutonomyLab() {
    }

    record Result(String ending, int bytesKept, long millis, int exit) {
    }

    /** Stops on time or output, whichever comes first, and kills the whole process tree. */
    static Result run(List<String> command, long timeoutMillis, int maxOutput, boolean tree)
            throws Exception {
        long start = System.nanoTime();
        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        int[] kept = new int[1];
        boolean[] overflow = new boolean[1];
        Thread reader = Thread.ofVirtual().start(() -> {
            byte[] buffer = new byte[8192];
            try (InputStream in = process.getInputStream()) {
                int n;
                while ((n = in.read(buffer)) >= 0) {
                    kept[0] = Math.min(maxOutput, kept[0] + n);
                    if (kept[0] >= maxOutput) {
                        overflow[0] = true;
                    }
                }
            } catch (Exception closed) {
                // The stream closes when the process is killed.
            }
        });
        String ending = "finished";
        long deadline = System.nanoTime() + timeoutMillis * 1_000_000;
        while (process.isAlive()) {
            if (overflow[0]) {
                ending = "OUTPUT LIMIT";
                break;
            }
            if (System.nanoTime() > deadline) {
                ending = "TIME LIMIT";
                break;
            }
            process.waitFor(20, TimeUnit.MILLISECONDS);
        }
        if (process.isAlive()) {
            if (tree) {
                process.descendants().forEach(ProcessHandle::destroyForcibly);
            }
            process.destroyForcibly();
            process.waitFor(2, TimeUnit.SECONDS);
        }
        reader.join(500);
        int exit = process.isAlive() ? -1 : process.exitValue();
        return new Result(ending, kept[0], (System.nanoTime() - start) / 1_000_000, exit);
    }

    static List<String> python(String code) {
        return List.of("python", "-I", "-c", code);
    }

    /** The parent starts a child, which writes a file later. Does the child outlive it? */
    static boolean childWrites(long killAfterMillis, boolean tree) throws Exception {
        Path dir = Files.createTempDirectory("agent");
        Path marker = dir.resolve("marker.txt");
        Path child = dir.resolve("child.py");
        Path parent = dir.resolve("parent.py");
        Files.writeString(child, "import time\ntime.sleep(3)\nopen(r'" + marker
                + "','w').write('x')\n");
        Files.writeString(parent, "import subprocess,sys,time\n"
                + "subprocess.Popen([sys.executable,'-I',r'" + child
                        + "'])\ntime.sleep(5)\n");
        run(List.of("python", "-I", parent.toString()), killAfterMillis, 1000, tree);
        Thread.sleep(4500);
        return Files.exists(marker);
    }

    /** Success of a run of steps, when each step works with probability p. */
    static void compounding() {
        System.out.println("Chance that every step of a run is right, if failures are "
                + "independent:");
        for (double p : new double[] {0.999, 0.99, 0.95}) {
            StringBuilder row = new StringBuilder(String.format("  each step %.1f%%:",
                    p * 100));
            for (int steps : new int[] {5, 20, 50, 200}) {
                row.append(String.format("  %3d steps %5.1f%%", steps,
                        100 * Math.pow(p, steps)));
            }
            System.out.println(row);
        }
    }

    enum Action { READ, DRAFT, WRITE_REVERSIBLE, SEND, PAY, DELETE }

    /** Who decides, based on how reversible the action is and how much is at stake. */
    static String decide(Action action, double amount, boolean sawUntrusted) {
        return switch (action) {
            case READ, DRAFT -> "AUTO";
            case WRITE_REVERSIBLE -> sawUntrusted ? "AUTO, logged and undoable" : "AUTO";
            case SEND -> sawUntrusted ? "ASK" : "AUTO, allowlisted recipients only";
            case PAY -> amount <= 50 && !sawUntrusted ? "AUTO, within budget" : "ASK";
            case DELETE -> "ASK";
        };
    }

    public static void main(String[] args) throws Exception {
        List<String[]> rows = new ArrayList<>();
        Result quick = run(python("print('hello')"), 3000, 100_000, true);
        rows.add(new String[] {"normal command", quick.ending(), "" + quick.bytesKept()});
        Result loop = run(python("while True: pass"), 1000, 100_000, true);
        rows.add(new String[] {"endless loop", loop.ending(), "" + loop.bytesKept()});
        Result flood = run(python("while True: print('x'*1000)"), 5000, 100_000, true);
        rows.add(new String[] {"output flood", flood.ending(), "" + flood.bytesKept()});
        System.out.println("Real child processes, 1 to 5 second limits, 100,000 byte "
                + "output cap:");
        for (String[] row : rows) {
            System.out.printf("  %-16s %-14s kept %,7d bytes%n", row[0], row[1],
                    Integer.parseInt(row[2]));
        }

        System.out.println();
        System.out.println("A parent starts a child that writes a file 3 seconds later:");
        System.out.println("  control, nothing killed: child wrote its file = "
                + childWrites(20_000, true));
        System.out.println("  kill only the parent at 1.5 s: child wrote its file = "
                + childWrites(1500, false));
        System.out.println("  kill the process tree at 1.5 s: child wrote its file = "
                + childWrites(1500, true));

        System.out.println();
        compounding();

        System.out.println();
        System.out.println("A sample policy by action:");
        System.out.println("  read                         " + decide(Action.READ, 0,
                true));
        System.out.println("  write, after untrusted input "
                + decide(Action.WRITE_REVERSIBLE, 0,
                true));
        System.out.println("  send, clean context          " + decide(Action.SEND, 0,
                false));
        System.out.println("  send, after untrusted input  " + decide(Action.SEND, 0,
                true));
        System.out.println("  pay $20, clean context       " + decide(Action.PAY, 20,
                false));
        System.out.println("  pay $20, after untrusted     " + decide(Action.PAY, 20,
                true));
        System.out.println("  pay $500                     " + decide(Action.PAY, 500,
                false));
        System.out.println("  delete                       " + decide(Action.DELETE, 0,
                false));
    }
}
