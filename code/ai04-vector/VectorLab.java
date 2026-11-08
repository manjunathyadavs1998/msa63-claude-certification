import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

/**
 * AI04 L2: measures IVF, quantization and filtering on synthetic clustered vectors.
 * Exact search over the same data provides the ground truth for recall.
 * Run: java VectorLab.java
 */
public class VectorLab {

    static final int N = 20_000;
    static final int DIM = 128;
    static final int CLUSTERS = 40;
    static final int QUERIES = 200;
    static final int K = 10;

    static float[][] data;
    static int[] clusterOf;
    static float[][] queries;
    static int[] queryCluster;
    static int[][] truth;
    static int[][] ranked;

    static float[] normalize(float[] v) {
        double sum = 0;
        for (float x : v) {
            sum += x * x;
        }
        float norm = (float) Math.sqrt(sum);
        for (int i = 0; i < v.length; i++) {
            v[i] /= norm;
        }
        return v;
    }

    static float dot(float[] a, float[] b) {
        float total = 0;
        for (int i = 0; i < a.length; i++) {
            total += a[i] * b[i];
        }
        return total;
    }

    static void generate() {
        Random random = new Random(42);
        float[][] centers = new float[CLUSTERS][DIM];
        for (float[] center : centers) {
            for (int d = 0; d < DIM; d++) {
                center[d] = (float) random.nextGaussian();
            }
            normalize(center);
        }
        data = new float[N][DIM];
        clusterOf = new int[N];
        for (int i = 0; i < N; i++) {
            clusterOf[i] = random.nextInt(CLUSTERS);
            data[i] = point(centers[clusterOf[i]], random);
        }
        queries = new float[QUERIES][DIM];
        queryCluster = new int[QUERIES];
        for (int q = 0; q < QUERIES; q++) {
            queryCluster[q] = random.nextInt(CLUSTERS);
            queries[q] = point(centers[queryCluster[q]], random);
        }
    }

    static float[] point(float[] center, Random random) {
        float[] v = new float[DIM];
        for (int d = 0; d < DIM; d++) {
            v[d] = center[d] + (float) (random.nextGaussian() * 0.05);
        }
        return normalize(v);
    }

    /** Returns candidate indexes, best first, among the given candidates. */
    static int[] topK(float[] query, int[] candidates, int k) {
        float[] scores = new float[N];
        for (int candidate : candidates) {
            scores[candidate] = dot(query, data[candidate]);
        }
        return Arrays.stream(candidates).boxed()
                .sorted(Comparator.comparingDouble(i -> -scores[i]))
                .limit(k)
                .mapToInt(Integer::intValue)
                .toArray();
    }

    static double recall(int[] found, int[] exact) {
        long hits = Arrays.stream(found)
                .filter(f -> Arrays.stream(exact).anyMatch(e -> e == f))
                .count();
        return (double) hits / exact.length;
    }

    /** IVF: k-means assigns vectors to lists, and a query scans only the closest lists. */
    static void ivf() {
        int lists = (int) Math.sqrt(N);
        Random random = new Random(7);
        float[][] centroids = new float[lists][];
        for (int c = 0; c < lists; c++) {
            centroids[c] = data[random.nextInt(N)].clone();
        }
        int[] assignment = new int[N];
        for (int iteration = 0; iteration < 5; iteration++) {
            final float[][] current = centroids;
            IntStream.range(0, N).parallel().forEach(i -> {
                int best = 0;
                float bestScore = -2;
                for (int c = 0; c < current.length; c++) {
                    float score = dot(data[i], current[c]);
                    if (score > bestScore) {
                        bestScore = score;
                        best = c;
                    }
                }
                assignment[i] = best;
            });
            float[][] next = new float[lists][DIM];
            for (int i = 0; i < N; i++) {
                for (int d = 0; d < DIM; d++) {
                    next[assignment[i]][d] += data[i][d];
                }
            }
            for (int c = 0; c < lists; c++) {
                boolean empty = true;
                for (float value : next[c]) {
                    empty &= value == 0;
                }
                centroids[c] = empty ? centroids[c] : normalize(next[c]);
            }
        }
        List<List<Integer>> members = new ArrayList<>();
        for (int c = 0; c < lists; c++) {
            members.add(new ArrayList<>());
        }
        for (int i = 0; i < N; i++) {
            members.get(assignment[i]).add(i);
        }
        System.out.printf("IVF with %d lists over %,d vectors%n", lists, N);
        for (int nprobe : new int[] {1, 2, 5, 10, 20}) {
            double totalRecall = 0;
            long scanned = 0;
            for (int q = 0; q < QUERIES; q++) {
                final float[] query = queries[q];
                final float[][] finalCentroids = centroids;
                int[] order = IntStream.range(0, lists).boxed()
                        .sorted(Comparator.comparingDouble(
                                c -> -dot(query, finalCentroids[c])))
                        .limit(nprobe)
                        .mapToInt(Integer::intValue)
                        .toArray();
                List<Integer> pool = new ArrayList<>();
                for (int c : order) {
                    pool.addAll(members.get(c));
                }
                scanned += pool.size();
                int[] candidates = pool.stream().mapToInt(Integer::intValue).toArray();
                totalRecall += recall(topK(query, candidates, K), truth[q]);
            }
            System.out.printf("  nprobe=%-3d recall@10=%.3f vectors scanned=%5.1f%%%n",
                    nprobe, totalRecall / QUERIES, 100.0 * scanned / (QUERIES * N));
        }
    }

    static byte[][] scalarQuantize() {
        byte[][] out = new byte[N][DIM];
        for (int i = 0; i < N; i++) {
            for (int d = 0; d < DIM; d++) {
                out[i][d] = quantize(data[i][d]);
            }
        }
        return out;
    }

    /** Maps the range -0.5 to 0.5 onto a signed byte. */
    static byte quantize(float value) {
        return (byte) Math.max(-127, Math.min(127, Math.round(value * 254)));
    }

    static long[][] binaryQuantize() {
        int words = (DIM + 63) / 64;
        long[][] out = new long[N][words];
        for (int i = 0; i < N; i++) {
            out[i] = bits(data[i]);
        }
        return out;
    }

    static long[] bits(float[] v) {
        long[] out = new long[(DIM + 63) / 64];
        for (int d = 0; d < DIM; d++) {
            if (v[d] > 0) {
                out[d / 64] |= 1L << (d % 64);
            }
        }
        return out;
    }

    static void quantization() {
        byte[][] sq = scalarQuantize();
        long[][] bq = binaryQuantize();
        double sqRecall = 0;
        double bqRecall = 0;
        double[] rerankRecall = new double[3];
        int[] pools = {10, 40, 100};
        for (int q = 0; q < QUERIES; q++) {
            float[] query = queries[q];
            byte[] qs = new byte[DIM];
            for (int d = 0; d < DIM; d++) {
                qs[d] = quantize(query[d]);
            }
            long[] qb = bits(query);
            int[] sqScores = new int[N];
            for (int i = 0; i < N; i++) {
                sqScores[i] = dotBytes(qs, sq[i]);
            }
            int[] sqTop = IntStream.range(0, N).boxed()
                    .sorted(Comparator.comparingInt(i -> -sqScores[i]))
                    .limit(K).mapToInt(Integer::intValue).toArray();
            sqRecall += recall(sqTop, truth[q]);
            Integer[] order = IntStream.range(0, N).boxed().toArray(Integer[]::new);
            int[] distance = new int[N];
            for (int i = 0; i < N; i++) {
                distance[i] = hamming(qb, bq[i]);
            }
            Arrays.sort(order, Comparator.comparingInt(i -> distance[i]));
            int[] binaryTop = Arrays.stream(order).limit(K).mapToInt(i -> i).toArray();
            bqRecall += recall(binaryTop, truth[q]);
            for (int p = 0; p < pools.length; p++) {
                int[] pool = Arrays.stream(order).limit(pools[p])
                        .mapToInt(i -> i).toArray();
                rerankRecall[p] += recall(topK(query, pool, K), truth[q]);
            }
        }
        long full = (long) N * DIM * 4;
        System.out.printf("%-34s %8s %10s%n", "Method", "Memory", "recall@10");
        System.out.printf("%-34s %8s %10.3f%n", "float32, exact", "100%", 1.0);
        System.out.printf("%-34s %7.1f%% %10.3f%n", "int8 scalar quantization",
                100.0 * N * DIM / full, sqRecall / QUERIES);
        System.out.printf("%-34s %7.1f%% %10.3f%n", "binary, Hamming only",
                100.0 * N * DIM / 8 / full, bqRecall / QUERIES);
        for (int p = 0; p < pools.length; p++) {
            System.out.printf("binary top %-3d then exact rerank %11s %10.3f%n",
                    pools[p], "*", rerankRecall[p] / QUERIES);
        }
        System.out.println(
                "* the rerank step also needs the float vectors, usually from disk");
    }

    static int dotBytes(byte[] a, byte[] b) {
        int total = 0;
        for (int i = 0; i < a.length; i++) {
            total += a[i] * b[i];
        }
        return total;
    }

    static int hamming(long[] a, long[] b) {
        int total = 0;
        for (int i = 0; i < a.length; i++) {
            total += Long.bitCount(a[i] ^ b[i]);
        }
        return total;
    }

    /** Post-filtering top-40 candidates, like an index scan followed by a WHERE clause. */
    static void filtering() {
        Random random = new Random(3);
        System.out.println("Filter independent of the vectors, 40 candidates, want 10");
        for (double pass : new double[] {0.5, 0.1, 0.01}) {
            boolean[] passes = new boolean[N];
            for (int i = 0; i < N; i++) {
                passes[i] = random.nextDouble() < pass;
            }
            report(pass, passes);
        }
        System.out.println("Filter tied to the vectors: a tenant owning 4 of 40 clusters");
        boolean[] tenant = new boolean[N];
        for (int i = 0; i < N; i++) {
            tenant[i] = clusterOf[i] < 4;
        }
        report(0.10, tenant);
    }

    static void report(double pass, boolean[] passes) {
        double matches = 0;
        int empty = 0;
        double scanned = 0;
        for (int q = 0; q < QUERIES; q++) {
            int[] order = ranked[q];
            int count = 0;
            for (int i = 0; i < 40; i++) {
                if (passes[order[i]]) {
                    count++;
                }
            }
            matches += Math.min(count, 40);
            empty += count == 0 ? 1 : 0;
            int found = 0;
            int walked = 0;
            while (found < K && walked < N) {
                if (passes[order[walked++]]) {
                    found++;
                }
            }
            scanned += walked;
        }
        System.out.printf("  pass=%-5.2f avg matches=%5.1f empty queries=%5.1f%% "
                        + "scan needed for 10=%,8.0f%n",
                pass, matches / QUERIES, 100.0 * empty / QUERIES, scanned / QUERIES);
    }

    public static void main(String[] args) {
        generate();
        int[] all = IntStream.range(0, N).toArray();
        truth = new int[QUERIES][];
        ranked = new int[QUERIES][];
        IntStream.range(0, QUERIES).parallel().forEach(q -> {
            ranked[q] = topK(queries[q], all, N);
            truth[q] = Arrays.copyOf(ranked[q], K);
        });
        ivf();
        quantization();
        filtering();
    }
}
