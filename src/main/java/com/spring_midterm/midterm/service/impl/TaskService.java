package com.spring_midterm.midterm.service.impl;

import com.spring_midterm.midterm.dto.request.GridRequest;
import com.spring_midterm.midterm.dto.response.PageResponse;
import com.spring_midterm.midterm.dto.request.TaskRequest;
import com.spring_midterm.midterm.dto.response.TaskResponse;
import com.spring_midterm.midterm.entity.Student;
import com.spring_midterm.midterm.entity.Task;
import com.spring_midterm.midterm.repository.IRepository;
import com.spring_midterm.midterm.service.AbstractCrudService;
import com.spring_midterm.midterm.service.ITaskService;
import com.spring_midterm.midterm.util.OwnershipValidator;
import com.spring_midterm.midterm.util.RepositoryUtils;
import com.spring_midterm.midterm.util.SpecificationHelper;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
public class TaskService extends AbstractCrudService<Task, TaskRequest, TaskResponse> implements ITaskService {

	private static final Map<String, String> SORT_FIELDS = Map.of(
			"id", "id",
			"title", "title",
			"name", "title",
			"description", "description",
			"completed", "completed",
			"dueDate", "dueDate",
			"studentId", "student.id",
			"studentName", "student.firstName"
	);

	private final IRepository<Student> studentRepository;

	public TaskService(IRepository<Task> taskRepository, IRepository<Student> studentRepository) {
		super(taskRepository);
		this.studentRepository = studentRepository;
	}

	@Transactional
	public TaskResponse create(Long studentId, TaskRequest request) {
		Task task = createEntity();
		updateTaskFields(task, studentId, request);
		log.info("Created task for student {}", studentId);
		return saveAndRespond(task);
	}

	public TaskResponse getById(Long studentId, Long taskId) {
		return toResponse(findTaskForStudent(studentId, taskId));
	}

	@Transactional
	public TaskResponse update(Long studentId, Long taskId, TaskRequest request) {
		Task task = findTaskForStudent(studentId, taskId);
		updateTaskFields(task, studentId, request);
		log.info("Updated task {} for student {}", taskId, studentId);
		return saveAndRespond(task);
	}

	@Transactional
	public void delete(Long studentId, Long taskId) {
		Task task = findTaskForStudent(studentId, taskId);
		repository.delete(task);
		log.info("Deleted task {} for student {}", taskId, studentId);
	}

	public PageResponse<TaskResponse> getGrid(Long studentId, GridRequest request) {
		Specification<Task> spec = SpecificationHelper.equalPath("student.id", studentId);
		String name = request.filters() != null ? request.filters().name() : null;
		if (name != null) {
			spec = spec.and(SpecificationHelper.likeIgnoreCase("title", name));
		}
		return findGrid(spec, request);
	}

	@Override
	protected Task createEntity() {
		return new Task();
	}

	@Override
	protected TaskResponse toResponse(Task task) {
		Student student = task.getStudent();
		return new TaskResponse(
				task.getId(),
				task.getTitle(),
				task.getDescription(),
				task.isCompleted(),
				task.getDueDate(),
				student.getId(),
				student.getFirstName() + " " + student.getLastName()
		);
	}

	@Override
	protected Map<String, String> getSortFields() {
		return SORT_FIELDS;
	}

	@Override
	protected String getResourceName() {
		return "task";
	}

	private Task findTaskForStudent(Long studentId, Long taskId) {
		return OwnershipValidator.validate(
				studentRepository, "student", studentId,
				repository, "task", taskId,
				task -> task.getStudent().getId());
	}

	private void updateTaskFields(Task task, Long studentId, TaskRequest request) {
		task.setTitle(request.title());
		task.setDescription(request.description());
		task.setCompleted(request.completed());
		task.setDueDate(request.dueDate());
		task.setStudent(RepositoryUtils.findOrThrow(studentRepository, studentId, "student"));
	}
}
