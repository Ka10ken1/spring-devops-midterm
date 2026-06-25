package com.spring_midterm.midterm.dto.response;

import java.time.LocalDateTime;

public record NoteResponse(
	Long id,
	String content,
	LocalDateTime createdAt,
	Long taskId,
	String taskTitle
) {
}
