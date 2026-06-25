package com.spring_midterm.midterm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NoteRequest(
		@NotBlank(message = "{validation.content.notblank}") @Size(min = 2, max = 1000, message = "{validation.content.size}") String content
) {
}
