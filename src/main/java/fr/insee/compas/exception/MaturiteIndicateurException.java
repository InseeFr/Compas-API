package fr.insee.compas.exception;

public class MaturiteIndicateurException extends RuntimeException {
    public MaturiteIndicateurException(String message, Throwable e) {
        super(message, e);
    }
    public MaturiteIndicateurException(String message) {
        super(message);
    }
}
