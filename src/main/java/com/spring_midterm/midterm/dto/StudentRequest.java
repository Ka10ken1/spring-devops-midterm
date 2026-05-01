package com.spring_midterm.midterm.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StudentRequest(
	@NotNull @Size(min = 2, max = 50) String firstName,
	@NotNull @Size(min = 2, max = 50) String lastName,
	@NotNull @Email @Size(max = 100) String email
) {
}
