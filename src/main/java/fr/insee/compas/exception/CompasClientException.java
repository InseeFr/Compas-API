package fr.insee.compas.exception;

public class CompasClientException extends CompasException {

    /** */
    private static final long serialVersionUID = 6455017330026470778L;

    public CompasClientException(int status, ErrorVM errorVM) {
        super(status, errorVM);
    }
}
