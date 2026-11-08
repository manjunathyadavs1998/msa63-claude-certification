package course.ai.agent;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/** Limits live in configuration, where the people who own them can see them. */
@Validated
@ConfigurationProperties("course.agent")
public record AgentProperties(
        @Min(1) int maxSteps,
        @Min(1000) long maxTokensPerRun,
        @NotNull BigDecimal refundLimit) {
}
