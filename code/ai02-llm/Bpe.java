import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** AI02 L1: byte-pair encoding over characters. Teaching code, not a real tokenizer. */
public class Bpe {

    static final String CORPUS = """
            refund refund refund refunded refunded refunds refund order order orders ordered
            return returned returns the the the the the the customer customer customers
            payment payment payments paid card card cards order refund the customer payment
            """;

    record Merge(String left, String right) {
    }

    static List<Merge> train(int merges) {
        Map<List<String>, Integer> words = new LinkedHashMap<>();
        for (String word : CORPUS.trim().split("\\s+")) {
            words.merge(symbols(word), 1, Integer::sum);
        }
        List<Merge> learned = new ArrayList<>();
        for (int round = 0; round < merges; round++) {
            Map<Merge, Integer> pairs = new HashMap<>();
            words.forEach((symbols, count) -> {
                for (int i = 0; i + 1 < symbols.size(); i++) {
                    Merge pair = new Merge(symbols.get(i), symbols.get(i + 1));
                    pairs.merge(pair, count, Integer::sum);
                }
            });
            if (pairs.isEmpty()) {
                break;
            }
            Merge best = pairs.entrySet().stream()
                    .max(Map.Entry.<Merge, Integer>comparingByValue()
                            .thenComparing(e -> e.getKey().left() + e.getKey().right()))
                    .orElseThrow()
                    .getKey();
            learned.add(best);
            Map<List<String>, Integer> updated = new LinkedHashMap<>();
            words.forEach((symbols, count) ->
                    updated.merge(apply(symbols, best), count, Integer::sum));
            words = updated;
        }
        return learned;
    }

    static List<String> symbols(String word) {
        List<String> out = new ArrayList<>();
        word.chars().forEach(c -> out.add(String.valueOf((char) c)));
        out.add("_");
        return out;
    }

    static List<String> apply(List<String> symbols, Merge merge) {
        List<String> out = new ArrayList<>();
        for (int i = 0; i < symbols.size(); i++) {
            boolean match = i + 1 < symbols.size()
                    && symbols.get(i).equals(merge.left())
                    && symbols.get(i + 1).equals(merge.right());
            if (match) {
                out.add(merge.left() + merge.right());
                i++;
            } else {
                out.add(symbols.get(i));
            }
        }
        return out;
    }

    static List<String> encode(String text, List<Merge> merges) {
        List<String> tokens = new ArrayList<>();
        for (String word : text.trim().split("\\s+")) {
            List<String> symbols = symbols(word);
            for (Merge merge : merges) {
                symbols = apply(symbols, merge);
            }
            tokens.addAll(symbols);
        }
        return tokens;
    }

    public static void main(String[] args) {
        List<Merge> merges = train(30);
        System.out.println("first merges:");
        merges.stream().limit(6)
                .forEach(m -> System.out.println("  " + m.left() + " + " + m.right()));
        List<String> words = List.of(
                "refund", "refunded", "refunding", "payment", "getCustomerById");
        for (String text : words) {
            List<String> tokens = encode(text, merges);
            System.out.printf("%-16s %2d tokens %s%n", text, tokens.size(), tokens);
        }
        List<String> samples = List.of(
                "the customer order refund", "{\"id\":\"A91X\",\"st\":2}");
        for (String text : samples) {
            System.out.printf("%2d chars -> %2d tokens: %s%n",
                    text.length(), encode(text, merges).size(), text);
        }
    }
}
