package fr.insee.compas.exception;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class CompasResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(CompasException.class)
    @ResponseBody
    public ResponseEntity<ErrorVM> processCompasException(
            CompasException e, HttpServletRequest request) {
        final BodyBuilder builder;
        builder = ResponseEntity.status(e.getStatus());
        return builder.body(e.getErrorVM());
    }
}
