package fr.insee.compas.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class CompasResponseEntityExceptionHandlerTest {

    private final CompasResponseEntityExceptionHandler handler =
            new CompasResponseEntityExceptionHandler();

    @Mock private HttpServletRequest httpServletRequest;

    @Test
    void testHandleApplicationException_returnsCorrectStatusAndBody() {

        final ErrorVM errorVM = new ErrorVM();
        errorVM.setCle("erreur.test");
        errorVM.setMessage("Un message d'erreur dans ce test");
        final CompasException exception = new CompasUploadException(400, errorVM);

        // When
        final ResponseEntity<ErrorVM> response =
                handler.processCompasException(exception, httpServletRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorVM, response.getBody());
    }
}
