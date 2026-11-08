package course.ai.web;

import course.ai.model.ModelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Log the detail for us, and send callers only what they can act on. */
@RestControllerAdvice
class ApiErrors {

    private static final Logger LOG = LoggerFactory.getLogger(ApiErrors.class);

    @ExceptionHandler(ModelException.class)
    ProblemDetail modelFailure(ModelException e) {
        LOG.error("Model call failed: kind={}", e.kind(), e);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.valueOf(e.kind().httpStatus),
                e.kind().retryable ? "The assistant is busy. Please try again shortly."
                        : "The assistant is unavailable.");
        problem.setProperty("kind", e.kind().name());
        problem.setProperty("retryable", e.kind().retryable);
        return problem;
    }
}
