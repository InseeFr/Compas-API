package fr.insee.compas.exception;

public class SonarApiException extends RuntimeException {
    public SonarApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
