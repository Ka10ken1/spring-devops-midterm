package com.spring_midterm.midterm.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record PageResponse<T>(
		List<T> data,
		@JsonProperty("page_index") int pageIndex,
		@JsonProperty("page_size") int pageSize,
		long totalElements,
		int totalPages
) {
}
