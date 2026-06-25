package com.spring_midterm.midterm.web.handler;

import com.spring_midterm.midterm.dto.response.ErrorResponse;
import com.spring_midterm.midterm.exception.ResourceNotFoundException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	private final MessageSource messageSource;

	public GlobalExceptionHandler(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException exception) {
		Locale locale = LocaleContextHolder.getLocale();
		String key = "error." + exception.getResource() + ".notfound";
		String message = messageSource.getMessage(key, new Object[]{exception.getIdentifier()}, exception.getMessage(), locale);
		log.warn("Resource not found: {} with id {}", exception.getResource(), exception.getIdentifier());
		return buildResponse(HttpStatus.NOT_FOUND, message);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
		Locale locale = LocaleContextHolder.getLocale();
		Map<String, String> errors = new LinkedHashMap<>();
		exception.getBindingResult().getFieldErrors()
				.forEach(error -> {
					String localized = messageSource.getMessage(error, locale);
					errors.put(error.getField(), localized);
				});

		String detail = messageSource.getMessage("error.validation.failed", new Object[]{errors}, locale);
		log.warn("Validation failed: {}", errors);
		return buildResponse(HttpStatus.BAD_REQUEST, detail);
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException exception) {
		Locale locale = LocaleContextHolder.getLocale();
		String message = resolveConflictMessage(exception, locale);
		log.error("Data integrity violation", exception);
		return buildResponse(HttpStatus.CONFLICT, message);
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException exception) {
		log.warn("Access denied: {}", exception.getMessage());
		Locale locale = LocaleContextHolder.getLocale();
		String message = messageSource.getMessage("error.access.denied", null, locale);
		return buildResponse(HttpStatus.FORBIDDEN, message);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGeneric(Exception exception) {
		log.error("Unhandled exception", exception);
		Locale locale = LocaleContextHolder.getLocale();
		String message = messageSource.getMessage("error.internal", null, locale);
		return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, message);
	}

	private String resolveConflictMessage(DataIntegrityViolationException exception, Locale locale) {
		String detail = exception.getMostSpecificCause().getMessage();
		if (detail != null && detail.toLowerCase().contains("email")) {
			return messageSource.getMessage("error.student.email.exists", null, locale);
		}
		return messageSource.getMessage("error.conflict.generic", null, locale);
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
