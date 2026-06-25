package com.spring_midterm.midterm.web.api;

import com.spring_midterm.midterm.dto.request.GridRequest;
import com.spring_midterm.midterm.dto.request.NoteRequest;
import com.spring_midterm.midterm.dto.response.NoteResponse;
import com.spring_midterm.midterm.dto.response.PageResponse;
import com.spring_midterm.midterm.service.IMetricsService;
import com.spring_midterm.midterm.service.INoteService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@RestController
@RequestMapping("/api/tasks/{taskId}/notes")
public class NoteController {

	private final INoteService noteService;
	private final IMetricsService metricsService;

	public NoteController(INoteService noteService, IMetricsService metricsService) {
		this.noteService = noteService;
		this.metricsService = metricsService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Create a note", description = "Adds a new note and attaches it to a task.")
	public NoteResponse createNote(@PathVariable Long taskId, @Valid @RequestBody NoteRequest request) {
		log.info("API request: create note for task {}", taskId);
		metricsService.incrementCounter("api.note.create");
		return noteService.create(taskId, request);
	}

	@PutMapping("/{noteId}")
	@Operation(summary = "Update a note", description = "Updates an existing note record.")
	public NoteResponse updateNote(
		@PathVariable Long taskId,
		@PathVariable Long noteId,
		@Valid @RequestBody NoteRequest request
	) {
		log.info("API request: update note {} for task {}", noteId, taskId);
		metricsService.incrementCounter("api.note.update");
		return noteService.update(taskId, noteId, request);
	}

	@DeleteMapping("/{noteId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Delete a note", description = "Deletes a note record by id.")
	public void deleteNote(@PathVariable Long taskId, @PathVariable Long noteId) {
		log.info("API request: delete note {} for task {}", noteId, taskId);
		metricsService.incrementCounter("api.note.delete");
		noteService.delete(taskId, noteId);
	}

	@PostMapping("/grid/paged")
	@Operation(summary = "Get note grid", description = "Returns paginated notes using filters and sorting.")
	public PageResponse<NoteResponse> getGrid(@PathVariable Long taskId, @Valid @RequestBody GridRequest request) {
		return noteService.getGrid(taskId, request);
	}

	@GetMapping("/{noteId}")
	@Operation(summary = "Get note by id", description = "Returns one note record by id.")
	public NoteResponse getNoteById(@PathVariable Long taskId, @PathVariable Long noteId) {
		return noteService.getById(taskId, noteId);
	}
}
