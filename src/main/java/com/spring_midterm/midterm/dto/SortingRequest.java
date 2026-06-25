package com.spring_midterm.midterm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record SortingRequest(
		@Min(-1) @Max(1)
		@Schema(description = "Use -1 for descending and 1 for ascending. If omitted, ascending is used.")
		Integer direction,
		@NotBlank
		String sortingname
) {
}
