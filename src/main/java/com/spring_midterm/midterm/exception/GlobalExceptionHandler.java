package com.spring_midterm.midterm.exception;

import com.spring_midterm.midterm.dto.ErrorResponse;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException exception) {
		return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
		Map<String, String> errors = new LinkedHashMap<>();
		exception.getBindingResult().getFieldErrors()
				.forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

		return buildResponse(
			HttpStatus.BAD_REQUEST,
			"Validation failed: " + errors
		);
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException exception) {
		return buildResponse(
			HttpStatus.CONFLICT,
			resolveConflictMessage(exception)
		);
	}

	private String resolveConflictMessage(DataIntegrityViolationException exception) {
		String message = exception.getMostSpecificCause().getMessage();
		if (message != null && message.toLowerCase().contains("email")) {
			return "Student email already exists";
		}

		return "Record already exists or violates a database constraint";
	}

	private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String message) {
		ErrorResponse response = new ErrorResponse(
			Instant.now(),
			status.value(),
			status.getReasonPhrase(),
			message
		);

		return ResponseEntity.status(status).body(response);
	}
}
