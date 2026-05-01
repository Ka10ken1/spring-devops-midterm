package com.spring_midterm.midterm.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record TaskRequest(
		@NotNull @Size(min = 2, max = 100) String title,
		@Size(max = 1000) String description,
		boolean completed,
		LocalDate dueDate
) {
}
