package com.spring_midterm.midterm.dto.response;

import java.time.Instant;

public record ErrorResponse(
		Instant timestamp,
		int status,
		String error,
		String message
) {
}
