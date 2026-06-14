package backend.academy.linktracker.scrapper.resilience;

public class RetryableHttpException extends RuntimeException {
    public RetryableHttpException(String message, Throwable cause) {
        super(message, cause);
    }
}
