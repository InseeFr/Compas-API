package fr.insee.compas.exception;

public class CsvSeparatorDetectionException extends Exception {
    public CsvSeparatorDetectionException(String message) {
        super(message);
    }

    public CsvSeparatorDetectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
