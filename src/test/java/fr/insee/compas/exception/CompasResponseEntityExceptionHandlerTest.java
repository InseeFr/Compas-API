package fr.insee.compas.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

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

    @Test
    void handleMethodArgumentNotValid_shouldReturnBadRequestWithMessages() {
        // Arrange
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        ObjectError error1 = new ObjectError("field1", "Le champ est obligatoire");
        ObjectError error2 = new ObjectError("field2", "La valeur est invalide");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(error1, error2));

        HttpHeaders headers = new HttpHeaders();
        WebRequest request = mock(WebRequest.class);

        // Act
        ResponseEntity<Object> response =
                handler.handleMethodArgumentNotValid(ex, headers, HttpStatus.BAD_REQUEST, request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("detail"))
                .isEqualTo("Le champ est obligatoire, La valeur est invalide");
    }

    @Test
    void handleMethodArgumentNotValid_shouldReturnBadRequestWithSingleMessage() {
        // Arrange
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        ObjectError error = new ObjectError("field1", "Le champ est obligatoire");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(error));

        // Act
        ResponseEntity<Object> response =
                handler.handleMethodArgumentNotValid(
                        ex, new HttpHeaders(), HttpStatus.BAD_REQUEST, mock(WebRequest.class));

        // Assert

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body).containsEntry("detail", "Le champ est obligatoire");
    }

    @Test
    void handleMethodArgumentNotValid_shouldReturnEmptyDetailWhenNoErrors() {
        // Arrange
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of());

        // Act
        ResponseEntity<Object> response =
                handler.handleMethodArgumentNotValid(
                        ex, new HttpHeaders(), HttpStatus.BAD_REQUEST, mock(WebRequest.class));

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body).containsEntry("detail", "");
    }
}
