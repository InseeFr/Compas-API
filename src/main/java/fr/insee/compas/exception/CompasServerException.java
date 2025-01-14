package fr.insee.compas.exception;

public class CompasServerException extends CompasException {

    /** */
    private static final long serialVersionUID = 3194116286570484093L;

    public CompasServerException(int status, ErrorVM errorVM) {
        super(status, errorVM);
    }
}
