package fr.insee.compas.exception;

public class JsonProcessingExceptionWrapper extends RuntimeException {
    public JsonProcessingExceptionWrapper(String message, Throwable cause) {
        super(message, cause);
    }
}
