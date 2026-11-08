import java.util.ArrayList;
import java.util.List;

/** AI04 L5: sizing a multi-tenant vector platform. Every input is a stated assumption. */
public class TenantPlan {

    static final long TOTAL_CHUNKS = 200_000_000L;
    static final int TENANTS = 5_000;
    static final int DIM = 1_024;
    static final double RAM_PER_NODE_GB = 128;
    static final double USABLE = 0.7;

    /** Tenant sizes follow a Zipf distribution, which is an assumption to validate. */
    static long[] tenantSizes() {
        double harmonic = 0;
        for (int i = 1; i <= TENANTS; i++) {
            harmonic += 1.0 / i;
        }
        long[] sizes = new long[TENANTS];
        for (int i = 1; i <= TENANTS; i++) {
            sizes[i - 1] = Math.round(TOTAL_CHUNKS / (harmonic * i));
        }
        return sizes;
    }

    static void tiers(long[] sizes) {
        System.out.printf(
                "Largest tenant: %,d chunks. Median tenant: %,d. Smallest: %,d%n",
                sizes[0], sizes[TENANTS / 2], sizes[TENANTS - 1]);
        for (long threshold : new long[] {100_000, 1_000_000, 10_000_000}) {
            int dedicated = 0;
            long inDedicated = 0;
            for (long size : sizes) {
                if (size >= threshold) {
                    dedicated++;
                    inDedicated += size;
                }
            }
            System.out.printf(
                    "threshold %,10d: %,5d dedicated tenants hold %4.1f%% of data, "
                            + "%,5d share the rest%n",
                    threshold, dedicated, 100.0 * inDedicated / TOTAL_CHUNKS,
                    TENANTS - dedicated);
        }
    }

    static void memory() {
        double floats = TOTAL_CHUNKS * DIM * 4 / 1e9;
        double int8 = floats / 4;
        double binary = floats / 32;
        // About 33 links per vector at M = 16, as derived in HLD M21A, with 8-byte IDs.
        double graph = TOTAL_CHUNKS * 33 * 8 / 1e9;
        List<String[]> rows = new ArrayList<>();
        rows.add(row("float32 + HNSW", floats + graph));
        rows.add(row("int8 + HNSW", int8 + graph));
        rows.add(row("binary + HNSW, floats on disk", binary + graph));
        System.out.printf("Vectors: float32 %,.0f GB, int8 %,.0f GB, binary %,.0f GB, "
                + "graph %,.0f GB%n", floats, int8, binary, graph);
        for (String[] r : rows) {
            System.out.printf("%-32s %8s GB in RAM  %3s nodes  %3s with 2 copies%n",
                    r[0], r[1], r[2], r[3]);
        }
    }

    static String[] row(String name, double gb) {
        int nodes = (int) Math.ceil(gb / (RAM_PER_NODE_GB * USABLE));
        return new String[] {name, String.format("%,.0f", gb), String.valueOf(nodes),
            String.valueOf(nodes * 2)};
    }

    public static void main(String[] args) {
        long[] sizes = tenantSizes();
        tiers(sizes);
        memory();

        double peakQps = 2_000;
        System.out.printf("Peak %,.0f queries/s: "
                + "fan-out to 3 shards means %,.0f shard searches/s, "
                + "routed to 1 shard: %,.0f%n", peakQps, peakQps * 3, peakQps);

        double chunksPerDay = 500_000;
        double perSecond = chunksPerDay / 86_400;
        System.out.printf("Ingest %,.0f chunks/day = %.1f/s on average%n",
                chunksPerDay, perSecond);
        for (int rate : new int[] {500, 2_000, 10_000}) {
            System.out.printf("Re-embed all at %,6d chunks/s: %.1f days%n",
                    rate, TOTAL_CHUNKS / (double) rate / 86_400);
        }
    }
}
