package com.spring_midterm.midterm.dto.response;

public record StudentResponse(
	Long id,
	String firstName,
	String lastName,
	String email
) {
}
