package course.ai.model;

/** Provider failures, translated into what the application should do about them. */
public class ModelException extends RuntimeException {

    public enum Kind {
        RATE_LIMITED(true, 429),
        OVERLOADED(true, 503),
        NETWORK(true, 503),
        BAD_REQUEST(false, 500),
        AUTH(false, 500),
        NOT_FOUND(false, 500),
        OTHER(false, 502);

        public final boolean retryable;
        public final int httpStatus;

        Kind(boolean retryable, int httpStatus) {
            this.retryable = retryable;
            this.httpStatus = httpStatus;
        }
    }

    private final Kind kind;

    public ModelException(Kind kind, String message, Throwable cause) {
        super(message, cause);
        this.kind = kind;
    }

    public Kind kind() {
        return kind;
    }
}
