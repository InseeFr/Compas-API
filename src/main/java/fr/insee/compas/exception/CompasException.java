package fr.insee.compas.exception;

public abstract class CompasException extends RuntimeException {
    /** */
    private static final long serialVersionUID = -7916902178497067599L;

    protected final int status;
    protected final ErrorVM errorVM;

    protected CompasException(int status, ErrorVM errorVM) {
        super(toMessage(status, errorVM));
        this.status = status;
        this.errorVM = errorVM;
    }

    private static String toMessage(int status, ErrorVM errorVM) {
        final StringBuilder msg = new StringBuilder("status : ").append(status);
        if (errorVM != null) {
            msg.append(" | cle : ").append(errorVM.getCle());
            msg.append(" | message : ").append(errorVM.getMessage());
        }
        return msg.toString();
    }

    public int getStatus() {
        return status;
    }

    public ErrorVM getErrorVM() {
        return errorVM;
    }
}
