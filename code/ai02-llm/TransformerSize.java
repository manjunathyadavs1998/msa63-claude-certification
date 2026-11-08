/** AI02 L3: counts every parameter in a GPT-2-style decoder-only transformer. */
public class TransformerSize {

    record Config(
            String name, long vocab, long context, long dim, long layers, long heads) {
    }

    static void report(Config c) {
        long d = c.dim();
        long tokenEmbedding = c.vocab() * d;
        long positionEmbedding = c.context() * d;
        // Query, key and value projections, then the output projection, with biases.
        long attention = (d * 3 * d + 3 * d) + (d * d + d);
        // Feed-forward layer: expands to 4d, then back to d, with biases.
        long feedForward = (d * 4 * d + 4 * d) + (4 * d * d + d);
        // Two layer norms per block, each with a scale and a shift per dimension.
        long norms = 2 * (2 * d);
        long block = attention + feedForward + norms;
        long finalNorm = 2 * d;
        // The output layer shares weights with the token embedding, so it adds nothing.
        long total = tokenEmbedding + positionEmbedding + c.layers() * block + finalNorm;

        System.out.printf("%s: d=%d, heads=%d, d_k=%d, layers=%d%n",
                c.name(), d, c.heads(), d / c.heads(), c.layers());
        System.out.printf("  token embeddings    %,15d (%4.1f%%)%n",
                tokenEmbedding, 100.0 * tokenEmbedding / total);
        System.out.printf("  position embeddings %,15d (%4.1f%%)%n",
                positionEmbedding, 100.0 * positionEmbedding / total);
        System.out.printf("  attention, all      %,15d (%4.1f%%)%n",
                c.layers() * attention, 100.0 * c.layers() * attention / total);
        System.out.printf("  feed-forward, all   %,15d (%4.1f%%)%n",
                c.layers() * feedForward, 100.0 * c.layers() * feedForward / total);
        System.out.printf("  layer norms         %,15d (%4.1f%%)%n",
                c.layers() * norms + finalNorm,
                100.0 * (c.layers() * norms + finalNorm) / total);
        System.out.printf("  total parameters    %,15d%n", total);
        System.out.printf("  memory at 32-bit %.2f GB, at 16-bit %.2f GB%n",
                total * 4 / 1e9, total * 2 / 1e9);
    }

    public static void main(String[] args) {
        report(new Config("GPT-2 small", 50_257, 1_024, 768, 12, 12));
        report(new Config("GPT-2 XL", 50_257, 1_024, 1_600, 48, 25));
    }
}
