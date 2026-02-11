package fr.insee.compas.exception;

public class GitLabException extends RuntimeException {
    public GitLabException(String message, Throwable cause) {
        super(message, cause);
    }
}
