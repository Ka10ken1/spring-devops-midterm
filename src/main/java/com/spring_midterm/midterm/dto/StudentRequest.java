package com.spring_midterm.midterm.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StudentRequest(
	@NotBlank(message = "{validation.firstname.notblank}") @Size(min = 2, max = 50, message = "{validation.firstname.size}") String firstName,
	@NotBlank(message = "{validation.lastname.notblank}") @Size(min = 2, max = 50, message = "{validation.lastname.size}") String lastName,
	@NotBlank(message = "{validation.email.notblank}") @Email(message = "{validation.email.invalid}") @Size(max = 100, message = "{validation.email.size}") String email
) {
}
