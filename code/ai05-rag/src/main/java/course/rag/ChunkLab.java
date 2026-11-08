package course.rag;

import java.util.ArrayList;
import java.util.List;

/**
 * AI05 L2: how chunking choices decide whether a fact survives in one piece.
 * The policy text and facts are invented. Sizes are in characters to keep this simple.
 */
public final class ChunkLab {

    private ChunkLab() {
    }

    static final String DOCUMENT = """
            # Refund Policy

            ## Eligibility
            Customers may request a refund within 30 days of delivery. Items must be \
            unused and in the original packaging. Gift cards are not eligible for a \
            refund under any circumstances.

            ## Damaged goods
            If an item arrives damaged, report it within 7 days and attach a photo. We \
            send a replacement within 2 business days. If no replacement is available, \
            we refund the full amount to the original payment method.

            ## Refund timing
            Card refunds take 5 to 10 business days to appear. Wallet refunds \
            appear within \
            24 hours. Bank transfers take up to 14 days because of clearing rules.

            ## Exceptions
            Orders above $5,000 require approval from a regional manager before any \
            refund is issued. Refunds for corporate accounts are credited to the account \
            balance and are never paid in cash.
            """;

    /** Each fact must be readable within one chunk to be useful for answering. */
    static final List<String> FACTS = List.of(
            "within 30 days of delivery",
            "Gift cards are not eligible for a refund under any circumstances",
            "report it within 7 days and attach a photo",
            "If no replacement is available, we refund the full amount",
            "Card refunds take 5 to 10 business days",
            "Wallet refunds appear within 24 hours",
            "Bank transfers take up to 14 days because of clearing rules",
            "Orders above $5,000 require approval from a regional manager",
            "Refunds for corporate accounts are credited to the account balance");

    static List<String> fixedSize(String text, int size, int overlap) {
        List<String> chunks = new ArrayList<>();
        int step = size - overlap;
        for (int start = 0; start < text.length(); start += step) {
            chunks.add(text.substring(start, Math.min(text.length(), start + size)));
            if (start + size >= text.length()) {
                break;
            }
        }
        return chunks;
    }

    /** Sections first, then sentences, so that nothing is cut mid-sentence. */
    static List<String> structured(String text, int maxSize) {
        List<String> chunks = new ArrayList<>();
        String title = "";
        for (String block : text.split("\n\n")) {
            if (block.startsWith("# ")) {
                title = block.substring(2).trim();
                continue;
            }
            String[] lines = block.split("\n", 2);
            String heading = title + " > " + lines[0].replace("## ", "").trim();
            String body = lines.length > 1 ? lines[1].trim() : "";
            StringBuilder current = new StringBuilder(heading).append(":");
            for (String sentence : body.split("(?<=\\.)\\s+")) {
                if (current.length() + sentence.length() + 1 > maxSize
                        && current.length() > heading.length() + 1) {
                    chunks.add(current.toString());
                    current = new StringBuilder(heading).append(":");
                }
                current.append(' ').append(sentence);
            }
            chunks.add(current.toString());
        }
        return chunks;
    }

    static long brokenFacts(List<String> chunks) {
        return FACTS.stream()
                .filter(fact -> chunks.stream().noneMatch(chunk -> chunk.contains(fact)))
                .count();
    }

    static long midSentenceEnds(List<String> chunks) {
        return chunks.stream()
                .map(String::stripTrailing)
                .filter(chunk -> !chunk.endsWith(".") && !chunk.endsWith(":"))
                .count();
    }

    static void report(String name, List<String> chunks) {
        int chars = chunks.stream().mapToInt(String::length).sum();
        System.out.printf("%-26s chunks=%2d stored=%4d chars broken facts=%d of %d "
                        + "cut mid-sentence=%d%n",
                name, chunks.size(), chars, brokenFacts(chunks), FACTS.size(),
                midSentenceEnds(chunks));
    }

    public static void main(String[] args) {
        System.out.printf("document=%d chars, facts=%d%n", DOCUMENT.length(), FACTS.size());
        report("fixed 100, no overlap", fixedSize(DOCUMENT, 100, 0));
        report("fixed 200, no overlap", fixedSize(DOCUMENT, 200, 0));
        report("fixed 200, 20% overlap", fixedSize(DOCUMENT, 200, 40));
        report("fixed 200, 50% overlap", fixedSize(DOCUMENT, 200, 100));
        report("fixed 400, no overlap", fixedSize(DOCUMENT, 400, 0));
        report("structured, max 200", structured(DOCUMENT, 200));
        report("structured, max 400", structured(DOCUMENT, 400));
        System.out.println("Sample structured chunk:");
        System.out.println("  " + structured(DOCUMENT, 400).get(2));
    }
}
