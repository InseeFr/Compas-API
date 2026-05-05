package fr.insee.compas.exception;

public class FilerErrorListenerException extends RuntimeException {
    public FilerErrorListenerException(Throwable e, String message) {
        super(message, e);
    }
}
