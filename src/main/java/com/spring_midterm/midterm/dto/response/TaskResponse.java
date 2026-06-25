package com.spring_midterm.midterm.dto.response;

import java.time.LocalDate;

public record TaskResponse(
		Long id,
		String title,
		String description,
		boolean completed,
		LocalDate dueDate,
		Long studentId,
		String studentName
) {
}
