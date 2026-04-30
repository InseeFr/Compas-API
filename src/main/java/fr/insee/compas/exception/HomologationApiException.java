package fr.insee.compas.exception;

public class HomologationApiException extends RuntimeException {
    public HomologationApiException(String message, Throwable erreur) {
        super(message, erreur);
    }
}
