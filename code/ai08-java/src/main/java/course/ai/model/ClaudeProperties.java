package course.ai.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/** Checked at startup, so a bad setting stops the deploy rather than the first customer. */
@Validated
@ConfigurationProperties("course.claude")
public record ClaudeProperties(
        @NotBlank String model,
        @Min(1) @Max(128000) long maxTokens,
        @NotNull Duration timeout,
        @Min(0) @Max(10) int maxRetries,
        @NotBlank String effort,
        String baseUrl,
        String apiKey) {
}
