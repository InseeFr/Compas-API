package fr.insee.compas.exception;

public class AnalyzerApiException extends RuntimeException {

    public AnalyzerApiException(String message) {
        super(message);
    }

    public AnalyzerApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
