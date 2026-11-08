package course.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * AI09 L4: personal data detection, secret scanning, cache keys, roles and a tamper-evident
 * log. All are sketches on invented data. The detector's misses are shown deliberately.
 */
public final class DataGuards {

    private DataGuards() {
    }

    private static final Pattern CARD = Pattern.compile("\\b\\d(?:[ -]?\\d){12,18}\\b");
    private static final Pattern EMAIL = Pattern.compile("[\\w.+-]+@[\\w-]+\\.[\\w.]+");
    private static final Pattern SECRET = Pattern.compile(
            "(sk-ant-[A-Za-z0-9_-]{6,}|AKIA[0-9A-Z]{16}|password\\s*=\\s*\\S+)");

    /** The Luhn check removes most long numbers. One random number in ten still passes. */
    static boolean luhn(String digits) {
        int sum = 0;
        boolean twice = false;
        for (int i = digits.length() - 1; i >= 0; i--) {
            int digit = digits.charAt(i) - '0';
            if (twice) {
                digit = digit * 2 > 9 ? digit * 2 - 9 : digit * 2;
            }
            sum += digit;
            twice = !twice;
        }
        return sum % 10 == 0;
    }

    static List<String> detect(String text) {
        List<String> found = new ArrayList<>();
        var cards = CARD.matcher(text);
        while (cards.find()) {
            if (luhn(cards.group().replaceAll("[ -]", ""))) {
                found.add("card");
            }
        }
        if (EMAIL.matcher(text).find()) {
            found.add("email");
        }
        if (SECRET.matcher(text).find()) {
            found.add("secret");
        }
        return found;
    }

    /** A chain of hashes: changing any old entry breaks every later one. */
    static final class AuditLog {
        private final List<String[]> entries = new ArrayList<>();

        void append(String event) throws Exception {
            String previous = entries.isEmpty() ? "start" : entries.getLast()[1];
            entries.add(new String[] {event, hash(previous + event)});
        }

        void tamper(int index, String event) {
            entries.get(index)[0] = event;
        }

        /** What an attacker with full write access does: recomputes every later hash. */
        void rewriteChain() throws Exception {
            String previous = "start";
            for (String[] entry : entries) {
                entry[1] = hash(previous + entry[0]);
                previous = entry[1];
            }
        }

        String head() {
            return entries.getLast()[1];
        }

        int firstBrokenEntry() throws Exception {
            String previous = "start";
            for (int i = 0; i < entries.size(); i++) {
                if (!hash(previous + entries.get(i)[0]).equals(entries.get(i)[1])) {
                    return i;
                }
                previous = entries.get(i)[1];
            }
            return -1;
        }

        private static String hash(String text) throws Exception {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(text.getBytes(StandardCharsets.UTF_8))).substring(0, 16);
        }
    }

    record User(String id, String tenant, Set<String> roles) {
    }

    record Doc(String id, String tenant, String requiredRole) {
    }

    /** Tenant first, then role. Deny by default. */
    static boolean mayRead(User user, Doc doc) {
        return user.tenant().equals(doc.tenant())
                && user.roles().contains(doc.requiredRole());
    }

    static String cacheKey(String tenant, String roles, String question) {
        return tenant + "|" + roles + "|" + question;
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Detection, on invented data:");
        Map<String, String> samples = new LinkedHashMap<>();
        samples.put("card, valid check digit", "Paid with 4111 1111 1111 1111 today");
        samples.put("order number, same length", "Order 4111 1111 1111 1112 shipped");
        samples.put("email", "Contact asha@example.com please");
        samples.put("api key", "key is sk-ant-abc123def456");
        samples.put("password in config", "db password = hunter2");
        samples.put("name and address", "Asha Rao lives at 12 MG Road, Pune");
        samples.put("card number spelled out", "four one one one, ending 1111");
        samples.put("national ID", "Aadhaar 2345 6789 0123");
        for (Map.Entry<String, String> sample : samples.entrySet()) {
            System.out.printf("  %-30s %s%n", sample.getKey(), detect(sample.getValue()));
        }

        System.out.println("Cache keys:");
        String question = "What's our refund policy?";
        System.out.println("  question only: both tenants get [" + question + "]");
        System.out.println("  scoped:        [" + cacheKey("acme", "support", question)
                + "]");
        System.out.println("                 [" + cacheKey("globex", "support", question)
                + "]");

        System.out.println("Access:");
        User asha = new User("asha", "acme", Set.of("finance"));
        for (Doc doc : List.of(new Doc("acme-close", "acme", "finance"),
                new Doc("acme-hr", "acme", "hr"),
                new Doc("globex-close", "globex", "finance"))) {
            System.out.printf("  asha reads %-14s %s%n", doc.id(), mayRead(asha, doc));
        }

        System.out.println("Audit log:");
        AuditLog log = new AuditLog();
        log.append("asha asked about order 7781");
        log.append("tool lookup_order ran");
        log.append("refund of 40.00 approved");
        String anchored = log.head();
        System.out.println("  intact, first broken entry: " + log.firstBrokenEntry());
        log.tamper(1, "tool lookup_order did not run");
        System.out.println("  edited, first broken entry: " + log.firstBrokenEntry());
        log.rewriteChain();
        System.out.println("  chain rewritten, first broken entry: "
                + log.firstBrokenEntry());
        System.out.println("  head still matches the copy kept elsewhere: "
                + log.head().equals(anchored));
    }
}
