package com.spring_midterm.midterm.controller;

import com.spring_midterm.midterm.dto.GridRequest;
import com.spring_midterm.midterm.dto.PageResponse;
import com.spring_midterm.midterm.dto.TaskRequest;
import com.spring_midterm.midterm.dto.TaskResponse;
import com.spring_midterm.midterm.service.ITaskService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/students/{studentId}/tasks")
public class TaskController {

	private final ITaskService taskService;

	public TaskController(ITaskService taskService) {
		this.taskService = taskService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Create a task", description = "Adds a new task and assigns it to a student.")
	public TaskResponse createTask(@PathVariable Long studentId, @Valid @RequestBody TaskRequest request) {
		return taskService.create(studentId, request);
	}

	@PutMapping("/{taskId}")
	@Operation(summary = "Update a task", description = "Updates an existing task record.")
	public TaskResponse updateTask(
			@PathVariable Long studentId,
			@PathVariable Long taskId,
			@Valid @RequestBody TaskRequest request
	) {
		return taskService.update(studentId, taskId, request);
	}

	@DeleteMapping("/{taskId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Delete a task", description = "Deletes a task record by id.")
	public void deleteTask(@PathVariable Long studentId, @PathVariable Long taskId) {
		taskService.delete(studentId, taskId);
	}

	@PostMapping("/grid/paged")
	@Operation(summary = "Get task grid", description = "Returns paginated tasks using filters and sorting.")
	public PageResponse<TaskResponse> getGrid(@PathVariable Long studentId, @Valid @RequestBody GridRequest request) {
		return taskService.getGrid(studentId, request);
	}

	@GetMapping("/{taskId}")
	@Operation(summary = "Get task by id", description = "Returns one task record by id.")
	public TaskResponse getTaskById(@PathVariable Long studentId, @PathVariable Long taskId) {
		return taskService.getById(studentId, taskId);
	}
}
