package fr.insee.compas.exception;

import org.springframework.stereotype.Component;

@Component
public class CompasExceptionResolver {

    private static final String CLE_ERROR = "demande.client.error";

    public String resolveExceptionMessage(Exception e) {

        return e instanceof final CompasException compasException
                        && compasException.getErrorVM() != null
                ? compasException.getErrorVM().getMessage()
                : CLE_ERROR;
    }
}
