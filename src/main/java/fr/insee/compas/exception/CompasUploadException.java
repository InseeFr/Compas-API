package fr.insee.compas.exception;

public class CompasUploadException extends CompasException {

    /** */
    private static final long serialVersionUID = 3194116286570484093L;

    public CompasUploadException(int status, ErrorVM errorVM) {
        super(status, errorVM);
    }
}
