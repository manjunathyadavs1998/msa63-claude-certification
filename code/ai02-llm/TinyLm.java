import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

/** AI02 L4 and L6: the smallest language model, a bigram counter on invented text. */
public class TinyLm {

    static final List<String> TRAIN = List.of(
            "refunds are approved within 14 days of delivery",
            "refunds are denied after 14 days of delivery",
            "gold customers receive free delivery within 2 days",
            "standard customers receive delivery within 7 days",
            "damaged orders are replaced within 2 days",
            "damaged orders receive refunds within 7 days",
            "card payments are refunded within 7 days",
            "wallet payments are refunded within 2 days",
            "orders are delivered within 7 days of payment");

    static final List<String> HOLDOUT = List.of(
            "standard orders are delivered within 7 days",
            "refunds are approved within 2 days of delivery");

    static final String START = "<s>";
    static final String END = "</s>";

    final Map<String, Map<String, Integer>> counts = new HashMap<>();
    final Set<String> vocabulary = new TreeSet<>();

    /** "Training" is self-supervised: each word's label is simply the word after it. */
    void train(List<String> sentences) {
        for (String sentence : sentences) {
            List<String> words = wrap(sentence);
            vocabulary.addAll(words);
            for (int i = 0; i + 1 < words.size(); i++) {
                counts.computeIfAbsent(words.get(i), w -> new HashMap<>())
                        .merge(words.get(i + 1), 1, Integer::sum);
            }
        }
    }

    static List<String> wrap(String sentence) {
        List<String> words = new ArrayList<>();
        words.add(START);
        words.addAll(Arrays.asList(sentence.split(" ")));
        words.add(END);
        return words;
    }

    /** Add-one smoothing, so that an unseen pair doesn't get a probability of zero. */
    double probability(String word, String next) {
        Map<String, Integer> row = counts.getOrDefault(word, Map.of());
        int total = row.values().stream().mapToInt(Integer::intValue).sum();
        return (row.getOrDefault(next, 0) + 1.0) / (total + vocabulary.size());
    }

    double crossEntropy(List<String> sentences) {
        double loss = 0;
        int n = 0;
        for (String sentence : sentences) {
            List<String> words = wrap(sentence);
            for (int i = 0; i + 1 < words.size(); i++) {
                loss -= Math.log(probability(words.get(i), words.get(i + 1)));
                n++;
            }
        }
        return loss / n;
    }

    String generate(Random random) {
        List<String> out = new ArrayList<>();
        String current = START;
        while (out.size() < 15) {
            Map<String, Integer> row = counts.get(current);
            int total = row.values().stream().mapToInt(Integer::intValue).sum();
            int pick = random.nextInt(total);
            String next = END;
            for (String candidate : new TreeSet<>(row.keySet())) {
                pick -= row.get(candidate);
                if (pick < 0) {
                    next = candidate;
                    break;
                }
            }
            if (next.equals(END)) {
                break;
            }
            out.add(next);
            current = next;
        }
        return String.join(" ", out);
    }

    public static void main(String[] args) {
        TinyLm model = new TinyLm();
        model.train(TRAIN);
        System.out.printf("vocabulary = %d, guessing loss = ln(V) = %.3f%n",
                model.vocabulary.size(), Math.log(model.vocabulary.size()));
        System.out.printf("cross-entropy on training text = %.3f%n",
                model.crossEntropy(TRAIN));
        System.out.printf("cross-entropy on holdout text  = %.3f%n",
                model.crossEntropy(HOLDOUT));

        System.out.println("P(next | 'refunds') = are: "
                + String.format("%.3f", model.probability("refunds", "are")));

        Random random = new Random(5);
        Set<String> seen = new TreeSet<>();
        int invented = 0;
        int total = 200;
        for (int i = 0; i < total; i++) {
            String sentence = model.generate(random);
            boolean original = TRAIN.contains(sentence);
            if (!original) {
                invented++;
            }
            if (seen.size() < 6 && seen.add(sentence)) {
                System.out.printf("%-9s %s%n", original ? "[in text]" : "[NEW]", sentence);
            }
        }
        System.out.printf("%d of %d generated sentences never appeared in training%n",
                invented, total);
    }
}
