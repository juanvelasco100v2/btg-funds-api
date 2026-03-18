package com.btg.fundmanagement.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleApiException_returnsCorrectStatusAndMessage() {
        var ex = new ApiException.FundNotFound("f1");

        var response = handler.handleApiException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().status());
        assertTrue(response.getBody().message().contains("f1"));
        assertNotNull(response.getBody().timestamp());
    }

    @Test
    void handleApiException_badRequest() {
        var ex = new ApiException.InsufficientBalance("FPV");

        var response = handler.handleApiException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().status());
    }

    @Test
    void handleApiException_conflict() {
        var ex = new ApiException.AlreadySubscribed("FPV");

        var response = handler.handleApiException(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(409, response.getBody().status());
    }

    @Test
    void handleApiException_unauthorized() {
        var ex = new ApiException.InvalidCredentials();

        var response = handler.handleApiException(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(401, response.getBody().status());
    }

    @Test
    void handleValidation_returnsBadRequestWithFieldErrors() {
        var bindingResult = mock(BindingResult.class);
        var fieldError = new FieldError("request", "email", "must not be blank");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));
        var ex = new MethodArgumentNotValidException(null, bindingResult);

        var response = handler.handleValidation(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().status());
        assertTrue(response.getBody().message().contains("email"));
        assertTrue(response.getBody().message().contains("must not be blank"));
    }

    @Test
    void handleValidation_multipleFieldErrors() {
        var bindingResult = mock(BindingResult.class);
        var error1 = new FieldError("request", "email", "must not be blank");
        var error2 = new FieldError("request", "password", "size must be at least 8");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(error1, error2));
        var ex = new MethodArgumentNotValidException(null, bindingResult);

        var response = handler.handleValidation(ex);

        assertTrue(response.getBody().message().contains("email"));
        assertTrue(response.getBody().message().contains("password"));
    }

    @Test
    void handleGeneral_returnsInternalServerError() {
        var ex = new RuntimeException("unexpected error");

        var response = handler.handleGeneral(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().status());
        assertEquals("Error interno del servidor", response.getBody().message());
    }
}
