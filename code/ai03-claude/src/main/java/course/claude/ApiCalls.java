package course.claude;

import com.anthropic.client.AnthropicClient;
import com.anthropic.errors.AnthropicIoException;
import com.anthropic.errors.AnthropicServiceException;
import com.anthropic.errors.BadRequestException;
import com.anthropic.errors.InternalServerException;
import com.anthropic.errors.RateLimitException;
import com.anthropic.errors.UnauthorizedException;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;

/** AI03 L1: catch a chain, so that retryable and permanent failures stay distinct. */
public final class ApiCalls {

    public static final class TryLaterException extends RuntimeException {
        TryLaterException(Throwable cause) {
            super(cause);
        }
    }

    public static final class ConfigurationException extends RuntimeException {
        ConfigurationException(Throwable cause) {
            super(cause);
        }
    }

    public static final class UnexpectedApiException extends RuntimeException {
        UnexpectedApiException(Throwable cause) {
            super(cause);
        }
    }

    private ApiCalls() {
    }

    public static Message create(AnthropicClient client, MessageCreateParams params) {
        try {
            return client.messages().create(params);
        } catch (RateLimitException e) {
            throw new TryLaterException(e);
        } catch (BadRequestException | UnauthorizedException e) {
            throw new ConfigurationException(e);
        } catch (InternalServerException | AnthropicIoException e) {
            throw new TryLaterException(e);
        } catch (AnthropicServiceException e) {
            throw new UnexpectedApiException(e);
        }
    }
}
