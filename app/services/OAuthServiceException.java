package services;

public class OAuthServiceException extends RuntimeException {
    public OAuthServiceException(String errorMessage) {
        super(errorMessage);
    }

    public String jsonMessage() {
        return String.format("{\"_status\": \"error\", \"_message\": \"%s\"}", this.getMessage());
    }
}
