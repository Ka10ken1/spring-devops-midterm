package com.spring_midterm.midterm.service;

import com.spring_midterm.midterm.dto.GridRequest;
import com.spring_midterm.midterm.dto.NoteRequest;
import com.spring_midterm.midterm.dto.NoteResponse;
import com.spring_midterm.midterm.dto.PageResponse;
import com.spring_midterm.midterm.entity.Note;
import com.spring_midterm.midterm.entity.Task;
import com.spring_midterm.midterm.helper.OwnershipValidator;
import com.spring_midterm.midterm.helper.RepositoryUtils;
import com.spring_midterm.midterm.helper.SpecificationHelper;
import com.spring_midterm.midterm.repository.IRepository;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
public class NoteService extends AbstractCrudService<Note, NoteRequest, NoteResponse> implements INoteService {

	private static final Map<String, String> SORT_FIELDS = Map.of(
			"id", "id",
			"content", "content",
			"name", "content",
			"createdAt", "createdAt",
			"taskId", "task.id",
			"taskTitle", "task.title"
	);

	private final IRepository<Task> taskRepository;

	public NoteService(IRepository<Note> noteRepository, IRepository<Task> taskRepository) {
		super(noteRepository);
		this.taskRepository = taskRepository;
	}

	@Transactional
	public NoteResponse create(Long taskId, NoteRequest request) {
		Note note = createEntity();
		note.setCreatedAt(LocalDateTime.now());
		updateNoteFields(note, taskId, request);
		log.info("Created note for task {}", taskId);
		return saveAndRespond(note);
	}

	public NoteResponse getById(Long taskId, Long noteId) {
		return toResponse(findNoteForTask(taskId, noteId));
	}

	@Transactional
	public NoteResponse update(Long taskId, Long noteId, NoteRequest request) {
		Note note = findNoteForTask(taskId, noteId);
		updateNoteFields(note, taskId, request);
		log.info("Updated note {} for task {}", noteId, taskId);
		return saveAndRespond(note);
	}

	@Transactional
	public void delete(Long taskId, Long noteId) {
		Note note = findNoteForTask(taskId, noteId);
		repository.delete(note);
		log.info("Deleted note {} for task {}", noteId, taskId);
	}

	public PageResponse<NoteResponse> getGrid(Long taskId, GridRequest request) {
		Specification<Note> spec = SpecificationHelper.equalPath("task.id", taskId);
		String name = request.filters() != null ? request.filters().name() : null;
		if (name != null) {
			spec = spec.and(SpecificationHelper.likeIgnoreCase("content", name));
		}
		return findGrid(spec, request);
	}

	@Override
	protected Note createEntity() {
		return new Note();
	}

	@Override
	protected NoteResponse toResponse(Note note) {
		Task task = note.getTask();
		return new NoteResponse(
				note.getId(),
				note.getContent(),
				note.getCreatedAt(),
				task.getId(),
				task.getTitle()
		);
	}

	@Override
	protected Map<String, String> getSortFields() {
		return SORT_FIELDS;
	}

	@Override
	protected String getResourceName() {
		return "note";
	}

	private Note findNoteForTask(Long taskId, Long noteId) {
		return OwnershipValidator.validate(
				taskRepository, "task", taskId,
				repository, "note", noteId,
				note -> note.getTask().getId());
	}

	private void updateNoteFields(Note note, Long taskId, NoteRequest request) {
		note.setContent(request.content());
		note.setTask(RepositoryUtils.findOrThrow(taskRepository, taskId, "task"));
	}
}
