package course.rag;

import java.util.List;
import java.util.Locale;

/** AI05 L1: a first router written as plain rules, which are testable and explainable. */
public final class QuestionRouter {

    enum Source { DOCUMENTS, LIVE_DATA, ANALYTICS }

    private QuestionRouter() {
    }

    static Source route(String question) {
        String q = question.toLowerCase(Locale.ROOT);
        if (q.matches(".*\\b(how many|total|average|sum of)\\b.*")) {
            return Source.ANALYTICS;
        }
        if (q.matches(".*\\b(my order|my balance|status of)\\b.*")) {
            return Source.LIVE_DATA;
        }
        return Source.DOCUMENTS;
    }

    public static void main(String[] args) {
        for (String question : List.of(
                "How many refunds did we issue last quarter?",
                "What's the status of order 7781?",
                "What does the leave policy say about adoption?",
                "What's the total notice period for managers?")) {
            System.out.printf("%-11s %s%n", route(question), question);
        }
    }
}
