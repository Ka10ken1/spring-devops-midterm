package com.spring_midterm.midterm.dto.request;

import jakarta.validation.constraints.Size;

public record GridFilterRequest(
		@Size(max = 100) String name
) {
}
