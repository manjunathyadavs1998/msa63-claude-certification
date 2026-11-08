package course.ai.banking;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Swaps account and card numbers for tokens before text reaches the model, and back again
 * afterwards. A teaching sketch: real systems use a vetted detector, and a token vault.
 */
public final class PiiTokenizer {

    private static final Pattern ACCOUNT = Pattern.compile("\\b\\d{9,18}\\b");
    private static final Pattern EMAIL = Pattern.compile("[\\w.+-]+@[\\w-]+\\.[\\w.]+");

    private final Map<String, String> tokenToValue = new HashMap<>();
    private final Map<String, String> valueToToken = new HashMap<>();

    public String mask(String text) {
        return replace(replace(text, ACCOUNT, "ACCOUNT"), EMAIL, "EMAIL");
    }

    public String unmask(String text) {
        String out = text;
        for (Map.Entry<String, String> entry : tokenToValue.entrySet()) {
            out = out.replace(entry.getKey(), entry.getValue());
        }
        return out;
    }

    private String replace(String text, Pattern pattern, String kind) {
        Matcher matcher = pattern.matcher(text);
        StringBuilder out = new StringBuilder();
        while (matcher.find()) {
            String token = valueToToken.computeIfAbsent(matcher.group(), value -> {
                String made = "<" + kind + "_" + (tokenToValue.size() + 1) + ">";
                tokenToValue.put(made, value);
                return made;
            });
            matcher.appendReplacement(out, Matcher.quoteReplacement(token));
        }
        matcher.appendTail(out);
        return out.toString();
    }
}
