package com.spring_midterm.midterm.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record NoteRequest(
		@NotNull @Size(min = 2, max = 1000) String content
) {
}
