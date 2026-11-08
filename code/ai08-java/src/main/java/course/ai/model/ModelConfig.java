package course.ai.model;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** One shared client, because each one holds its own HTTP connection pool. */
@Configuration
public class ModelConfig {

    @Bean(destroyMethod = "close")
    @ConditionalOnProperty("course.claude.api-key")
    AnthropicClient anthropicClient(ClaudeProperties properties) {
        AnthropicOkHttpClient.Builder builder = AnthropicOkHttpClient.builder()
                .apiKey(properties.apiKey())
                .timeout(properties.timeout())
                .maxRetries(properties.maxRetries());
        if (properties.baseUrl() != null) {
            builder.baseUrl(properties.baseUrl());
        }
        return builder.build();
    }

    @Bean
    @ConditionalOnProperty("course.claude.api-key")
    ModelPort claudeModel(AnthropicClient client, ClaudeProperties properties) {
        return new ClaudeModel(client, properties);
    }

    /** With no key configured, the app starts on a script and says so in its answers. */
    @Bean
    @ConditionalOnMissingBean(ModelPort.class)
    ModelPort offlineModel() {
        return new ScriptedModel(request -> ScriptedModel.text(
                "[offline: no API key is set, so this answer is scripted] "
                        + "Your order 7781 was delivered on 3 November."), 40);
    }
}
