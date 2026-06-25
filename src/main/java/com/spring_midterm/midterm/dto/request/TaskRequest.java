package com.spring_midterm.midterm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record TaskRequest(
		@NotBlank(message = "{validation.title.notblank}") @Size(min = 2, max = 100, message = "{validation.title.size}") String title,
		@Size(max = 1000, message = "{validation.description.size}") String description,
		boolean completed,
		LocalDate dueDate
) {
}
