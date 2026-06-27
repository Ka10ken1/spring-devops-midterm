package com.spring_midterm.midterm.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record GridRequest(
		@JsonProperty("page_index") @NotNull @Min(0) Integer pageIndex,
		@JsonProperty("page_size") @Min(1) Integer pageSize,
		@Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED) @Valid GridFilterRequest filters,
		@Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
		@Valid SortingRequest sorting
) {
}
