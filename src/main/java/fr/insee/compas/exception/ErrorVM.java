package fr.insee.compas.exception;

import java.io.Serializable;

public class ErrorVM implements Serializable {

    /** */
    private static final long serialVersionUID = -8051650077520249748L;

    public static final String ERR_INTERNAL_SERVER_ERROR = "error.internalServerError";

    private String cle;

    private String message;

    public String getCle() {
        return cle;
    }

    public void setCle(String cle) {
        this.cle = cle;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((cle == null) ? 0 : cle.hashCode());
        result = prime * result + ((message == null) ? 0 : message.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ErrorVM other = (ErrorVM) obj;
        if (cle == null) {
            if (other.cle != null) {
                return false;
            }
        } else if (!cle.equals(other.cle)) {
            return false;
        }
        if (message == null) {
            if (other.message != null) {
                return false;
            }
        } else if (!message.equals(other.message)) {
            return false;
        }
        return true;
    }
}
