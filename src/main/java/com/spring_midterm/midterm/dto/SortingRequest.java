package com.spring_midterm.midterm.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record SortingRequest(
		@Schema(description = "Use -1 for descending and 1 for ascending. If omitted, ascending is used.")
		Integer direction,
		String sortingname
) {
}
