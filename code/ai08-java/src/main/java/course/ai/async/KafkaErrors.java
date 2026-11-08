package course.ai.async;

import course.ai.model.ModelException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

/** Retry only what could succeed. A bad request goes straight to the dead letter topic. */
@Configuration
class KafkaErrors {

    @Bean
    DefaultErrorHandler kafkaErrorHandler(KafkaOperations<Object, Object> kafka) {
        DefaultErrorHandler handler = new DefaultErrorHandler(
                new DeadLetterPublishingRecoverer(kafka), new FixedBackOff(200L, 2L));
        handler.addNotRetryableExceptions(PermanentFailure.class);
        return handler;
    }

    /** Marks events a retry can't fix, wrapped by the listener's caller. */
    static class PermanentFailure extends RuntimeException {
        PermanentFailure(ModelException cause) {
            super(cause);
        }
    }
}
