import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;
import java.util.stream.IntStream;

/**
 * AI04 L1: dimension trade-offs, the model-mismatch failure, and index sizing.
 * Vectors are synthetic. Early dimensions carry more variance, like a model trained
 * so that its vectors can be shortened. This isn't an actual embedding model.
 */
public class EmbeddingOps {

    static final int N = 10_000;
    static final int FULL_DIM = 1024;
    static final int QUERIES = 100;
    static final int K = 10;

    static float[] normalize(float[] v) {
        double sum = 0;
        for (float x : v) {
            sum += x * x;
        }
        float norm = (float) Math.sqrt(sum);
        float[] out = new float[v.length];
        for (int i = 0; i < v.length; i++) {
            out[i] = v[i] / norm;
        }
        return out;
    }

    static float dot(float[] a, float[] b) {
        float total = 0;
        for (int i = 0; i < a.length; i++) {
            total += a[i] * b[i];
        }
        return total;
    }

    static float[][] generate(int count, long seed) {
        Random random = new Random(seed);
        float[][] out = new float[count][FULL_DIM];
        for (float[] row : out) {
            for (int d = 0; d < FULL_DIM; d++) {
                row[d] = (float) (random.nextGaussian() * Math.pow(d + 1, -0.75));
            }
        }
        return out;
    }

    static float[][] truncate(float[][] vectors, int dim) {
        float[][] out = new float[vectors.length][];
        for (int i = 0; i < vectors.length; i++) {
            // Shortened vectors must be normalized again.
            out[i] = normalize(Arrays.copyOf(vectors[i], dim));
        }
        return out;
    }

    static int[] topK(float[] query, float[][] docs) {
        return IntStream.range(0, docs.length).boxed()
                .sorted(Comparator.comparingDouble(i -> -dot(query, docs[i])))
                .limit(K)
                .mapToInt(Integer::intValue)
                .toArray();
    }

    static double overlap(int[] a, int[] b) {
        return Arrays.stream(a)
                .filter(x -> Arrays.stream(b).anyMatch(y -> y == x))
                .count() / (double) K;
    }

    /** A different model is modeled by shuffling dimensions: a different space entirely. */
    static float[][] otherModel(float[][] vectors, long seed) {
        int[] permutation = IntStream.range(0, FULL_DIM).toArray();
        Random random = new Random(seed);
        for (int i = permutation.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int swap = permutation[i];
            permutation[i] = permutation[j];
            permutation[j] = swap;
        }
        float[][] out = new float[vectors.length][FULL_DIM];
        for (int i = 0; i < vectors.length; i++) {
            for (int d = 0; d < FULL_DIM; d++) {
                out[i][permutation[d]] = vectors[i][d];
            }
        }
        return out;
    }

    public static void main(String[] args) {
        float[][] docs = generate(N, 1);
        float[][] queries = generate(QUERIES, 2);
        float[][] fullDocs = truncate(docs, FULL_DIM);
        float[][] fullQueries = truncate(queries, FULL_DIM);
        int[][] truth = new int[QUERIES][];
        IntStream.range(0, QUERIES).parallel()
                .forEach(q -> truth[q] = topK(fullQueries[q], fullDocs));

        System.out.println(
                "Shortening vectors (synthetic data, recall against full length)");
        for (int dim : new int[] {1024, 512, 256, 128, 64}) {
            float[][] d = truncate(docs, dim);
            float[][] q = truncate(queries, dim);
            double recall = IntStream.range(0, QUERIES).parallel()
                    .mapToDouble(i -> overlap(topK(q[i], d), truth[i]))
                    .average().orElse(0);
            System.out.printf("  dim=%-5d recall@10=%.3f size=%5.1f%% of full%n",
                    dim, recall, 100.0 * dim / FULL_DIM);
        }

        float[][] mismatchedDocs = truncate(otherModel(docs, 9), FULL_DIM);
        double mismatch = IntStream.range(0, QUERIES).parallel()
                .mapToDouble(i -> overlap(topK(fullQueries[i], mismatchedDocs), truth[i]))
                .average().orElse(0);
        System.out.printf(
                "Query from model A against documents from model B: recall@10=%.3f%n",
                mismatch);
        System.out.printf("Chance level for top 10 of %,d: %.3f%n", N, (double) K / N);

        System.out.println("Raw vector storage for 100 million chunks");
        for (int dim : new int[] {256, 512, 1024, 2048}) {
            double gib = 100e6 * dim * 4 / 1e9;
            System.out.printf(
                    "  dim=%-5d float32=%,7.0f GB  int8=%,6.0f GB  binary=%,5.0f GB%n",
                    dim, gib, gib / 4, gib / 32);
        }
    }
}
