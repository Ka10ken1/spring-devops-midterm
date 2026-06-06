package com.spring_midterm.midterm.service;

import com.spring_midterm.midterm.dto.GridRequest;
import com.spring_midterm.midterm.dto.NoteRequest;
import com.spring_midterm.midterm.dto.NoteResponse;
import com.spring_midterm.midterm.dto.PageResponse;
import com.spring_midterm.midterm.entity.Note;
import com.spring_midterm.midterm.entity.Task;
import com.spring_midterm.midterm.exception.ResourceNotFoundException;
import com.spring_midterm.midterm.repository.IRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class NoteService implements INoteService {

	private static final Map<String, String> SORT_FIELDS = Map.of(
			"id", "id",
			"content", "content",
			"name", "content",
			"createdAt", "createdAt",
			"taskId", "task.id",
			"taskTitle", "task.title"
	);

	private final IRepository<Note> noteRepository;
	private final IRepository<Task> taskRepository;

	public NoteService(IRepository<Note> noteRepository, IRepository<Task> taskRepository) {
		this.noteRepository = noteRepository;
		this.taskRepository = taskRepository;
	}

	@Override
	public NoteResponse create(Long taskId, NoteRequest request) {
		Note note = new Note();
		note.setCreatedAt(LocalDateTime.now());
		updateNoteFields(note, taskId, request);
		return toResponse(noteRepository.save(note));
	}

	@Override
	public PageResponse<NoteResponse> getGrid(Long taskId, GridRequest request) {
		findTask(taskId);
		Pageable pageable = buildPageable(request);
		Page<Note> notePage = noteRepository.findAll(buildSpecification(taskId, request), pageable);
		List<NoteResponse> items = notePage.getContent()
				.stream()
				.map(this::toResponse)
				.toList();

		log.debug("Note grid query for task {} returned {} results", taskId, items.size());
		return new PageResponse<>(
				items,
				notePage.getNumber(),
				notePage.getSize(),
				notePage.getTotalElements(),
				notePage.getTotalPages()
		);
	}

	@Override
	public NoteResponse getById(Long taskId, Long noteId) {
		return toResponse(findNoteForTask(taskId, noteId));
	}

	@Override
	public NoteResponse update(Long taskId, Long noteId, NoteRequest request) {
		Note note = findNoteForTask(taskId, noteId);
		updateNoteFields(note, taskId, request);
		return toResponse(noteRepository.save(note));
	}

	@Override
	public void delete(Long taskId, Long noteId) {
		Note note = findNoteForTask(taskId, noteId);
		noteRepository.delete(note);
	}

	private Note findNote(Long id) {
		return noteRepository.findById(id)
				.orElseThrow(() -> {
					log.warn("Note with id {} not found", id);
					return new ResourceNotFoundException("note", id);
				});
	}

	private Task findTask(Long id) {
		return taskRepository.findById(id)
				.orElseThrow(() -> {
					log.warn("Task with id {} not found", id);
					return new ResourceNotFoundException("task", id);
				});
	}

	private Note findNoteForTask(Long taskId, Long noteId) {
		findTask(taskId);
		Note note = findNote(noteId);
		if (!note.getTask().getId().equals(taskId)) {
			log.warn("Note with id {} not found for task {}", noteId, taskId);
			throw new ResourceNotFoundException("note", noteId + " for task " + taskId);
		}
		return note;
	}

	private Specification<Note> buildSpecification(Long taskId, GridRequest request) {
		return (root, query, criteriaBuilder) -> {
			var taskPredicate = criteriaBuilder.equal(root.get("task").get("id"), taskId);
			if (request.filters() == null || !StringUtils.hasText(request.filters().name())) {
				return taskPredicate;
			}

			String name = "%" + request.filters().name().toLowerCase() + "%";
			return criteriaBuilder.and(
					taskPredicate,
					criteriaBuilder.like(criteriaBuilder.lower(root.get("content")), name)
			);
		};
	}

	private Pageable buildPageable(GridRequest request) {
		Sort sort = buildSort(request);
		if (request.pageSize() == null) {
			return Pageable.unpaged(sort);
		}

		return PageRequest.of(request.pageIndex(), request.pageSize(), sort);
	}

	private Sort buildSort(GridRequest request) {
		if (request.sorting() == null || !StringUtils.hasText(request.sorting().sortingname())) {
			return Sort.by(Sort.Direction.ASC, "id");
		}

		String sortField = SORT_FIELDS.getOrDefault(request.sorting().sortingname(), "id");
		Sort.Direction direction = request.sorting().direction() != null && request.sorting().direction() == -1
				? Sort.Direction.DESC
				: Sort.Direction.ASC;
		return Sort.by(direction, sortField);
	}

	private void updateNoteFields(Note note, Long taskId, NoteRequest request) {
		note.setContent(request.content());
		note.setTask(findTask(taskId));
	}

	private NoteResponse toResponse(Note note) {
		Task task = note.getTask();
		return new NoteResponse(
				note.getId(),
				note.getContent(),
				note.getCreatedAt(),
				task.getId(),
				task.getTitle()
		);
	}
}
