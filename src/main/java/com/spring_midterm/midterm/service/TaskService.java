package com.spring_midterm.midterm.service;

import com.spring_midterm.midterm.dto.GridRequest;
import com.spring_midterm.midterm.dto.PageResponse;
import com.spring_midterm.midterm.dto.TaskRequest;
import com.spring_midterm.midterm.dto.TaskResponse;
import com.spring_midterm.midterm.entity.Student;
import com.spring_midterm.midterm.entity.Task;
import com.spring_midterm.midterm.exception.ResourceNotFoundException;
import com.spring_midterm.midterm.repository.IRepository;
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
public class TaskService implements ITaskService {

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

	private final IRepository<Task> taskRepository;
	private final IRepository<Student> studentRepository;

	public TaskService(IRepository<Task> taskRepository, IRepository<Student> studentRepository) {
		this.taskRepository = taskRepository;
		this.studentRepository = studentRepository;
	}

	@Override
	public TaskResponse create(Long studentId, TaskRequest request) {
		Task task = new Task();
		updateTaskFields(task, studentId, request);
		return toResponse(taskRepository.save(task));
	}

	@Override
	public PageResponse<TaskResponse> getGrid(Long studentId, GridRequest request) {
		findStudent(studentId);
		Pageable pageable = buildPageable(request);

		Page<Task> taskPage = taskRepository.findAll(buildSpecification(studentId, request), pageable);

		List<TaskResponse> items = taskPage.getContent()
				.stream()
				.map(this::toResponse)
				.toList();

		log.debug("Task grid query for student {} returned {} results", studentId, items.size());
		return new PageResponse<>(
				items,
				taskPage.getNumber(),
				taskPage.getSize(),
				taskPage.getTotalElements(),
				taskPage.getTotalPages()
		);
	}

	@Override
	public TaskResponse getById(Long studentId, Long taskId) {
		return toResponse(findTaskForStudent(studentId, taskId));
	}

	@Override
	public TaskResponse update(Long studentId, Long taskId, TaskRequest request) {
		Task task = findTaskForStudent(studentId, taskId);
		updateTaskFields(task, studentId, request);
		return toResponse(taskRepository.save(task));
	}

	@Override
	public void delete(Long studentId, Long taskId) {
		Task task = findTaskForStudent(studentId, taskId);
		taskRepository.delete(task);
	}

	private Task findTask(Long id) {
		return taskRepository.findById(id)
				.orElseThrow(() -> {
					log.warn("Task with id {} not found", id);
					return new ResourceNotFoundException("task", id);
				});
	}

	private Student findStudent(Long id) {
		return studentRepository.findById(id)
				.orElseThrow(() -> {
					log.warn("Student with id {} not found", id);
					return new ResourceNotFoundException("student", id);
				});
	}

	private Task findTaskForStudent(Long studentId, Long taskId) {
		findStudent(studentId);
		Task task = findTask(taskId);
		if (!task.getStudent().getId().equals(studentId)) {
			log.warn("Task with id {} not found for student {}", taskId, studentId);
			throw new ResourceNotFoundException("task", taskId + " for student " + studentId);
		}
		return task;
	}

	private Pageable buildPageable(GridRequest request) {
		Sort sort = buildSort(request);
		if (request.pageSize() == null) {
			return Pageable.unpaged(sort);
		}

		return PageRequest.of(request.pageIndex(), request.pageSize(), sort);
	}

	private Specification<Task> buildSpecification(Long studentId, GridRequest request) {
		return (root, query, criteriaBuilder) -> {
			var studentPredicate = criteriaBuilder.equal(root.get("student").get("id"), studentId);
			if (request.filters() == null || !StringUtils.hasText(request.filters().name())) {
				return studentPredicate;
			}

			String name = "%" + request.filters().name().toLowerCase() + "%";
			return criteriaBuilder.and(
					studentPredicate,
					criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), name)
			);
		};
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

	private void updateTaskFields(Task task, Long studentId, TaskRequest request) {
		task.setTitle(request.title());
		task.setDescription(request.description());
		task.setCompleted(request.completed());
		task.setDueDate(request.dueDate());
		task.setStudent(findStudent(studentId));
	}

	private TaskResponse toResponse(Task task) {
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
}
