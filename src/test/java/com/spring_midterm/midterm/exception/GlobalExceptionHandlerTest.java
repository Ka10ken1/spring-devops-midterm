package com.spring_midterm.midterm.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.spring_midterm.midterm.dto.ErrorResponse;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private MessageSource messageSource;

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler(messageSource);
    }

    @Test
    void handleResourceNotFound_shouldReturn404() {
        ResourceNotFoundException ex = new ResourceNotFoundException("student", 42L);
        when(messageSource.getMessage(eq("error.student.notfound"), any(Object[].class), any(String.class), any(Locale.class)))
                .thenReturn("Student with id 42 was not found");

        ResponseEntity<ErrorResponse> response = handler.handleResourceNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().status());
        assertEquals("Student with id 42 was not found", response.getBody().message());
    }

    @Test
    void handleValidation_shouldReturn400() {
        FieldError fieldError = new FieldError("student", "firstName", "First name is required");
        BindingResult bindingResult = org.mockito.Mockito.mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        MethodArgumentNotValidException ex = org.mockito.Mockito.mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        when(messageSource.getMessage(any(FieldError.class), any(Locale.class)))
                .thenReturn("First name is required");
        when(messageSource.getMessage(eq("error.validation.failed"), any(Object[].class), any(Locale.class)))
                .thenReturn("Validation failed: {firstName=First name is required}");

        ResponseEntity<ErrorResponse> response = handler.handleValidation(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().status());
    }

    @Test
    void handleDataIntegrityViolation_withEmailConflict_shouldReturn409() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException(
                "ERROR: duplicate key value violates unique constraint \"email\""
        );
        when(messageSource.getMessage(eq("error.student.email.exists"), any(), any(Locale.class)))
                .thenReturn("Student email already exists");

        ResponseEntity<ErrorResponse> response = handler.handleDataIntegrityViolation(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().status());
        assertEquals("Student email already exists", response.getBody().message());
    }

    @Test
    void handleDataIntegrityViolation_genericConflict_shouldReturn409() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException(
                "some other constraint violation"
        );
        when(messageSource.getMessage(eq("error.conflict.generic"), any(), any(Locale.class)))
                .thenReturn("Record already exists or violates a database constraint");

        ResponseEntity<ErrorResponse> response = handler.handleDataIntegrityViolation(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().status());
        assertEquals("Record already exists or violates a database constraint", response.getBody().message());
    }
}
