package com.spring_midterm.midterm.service;

import com.spring_midterm.midterm.dto.GridRequest;
import com.spring_midterm.midterm.dto.PageResponse;
import com.spring_midterm.midterm.dto.StudentRequest;
import com.spring_midterm.midterm.dto.StudentResponse;
import com.spring_midterm.midterm.entity.Student;
import com.spring_midterm.midterm.exception.ResourceNotFoundException;
import com.spring_midterm.midterm.repository.IRepository;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class StudentService implements IService<StudentRequest, StudentResponse> {

	private static final Map<String, String> SORT_FIELDS = Map.of(
			"id", "id",
			"firstName", "firstName",
			"lastName", "lastName",
			"name", "firstName",
			"email", "email"
	);

	private final IRepository<Student> studentRepository;

	public StudentService(IRepository<Student> studentRepository) {
		this.studentRepository = studentRepository;
	}

	@Override
	public StudentResponse create(StudentRequest request) {
		Student student = new Student();
		updateStudentFields(student, request);
		return toResponse(studentRepository.save(student));
	}

	@Override
	public PageResponse<StudentResponse> getGrid(GridRequest request) {
		Pageable pageable = buildPageable(request);
		Page<Student> studentPage = studentRepository.findAll(buildSpecification(request), pageable);
		List<StudentResponse> items = studentPage.getContent()
				.stream()
				.map(this::toResponse)
				.toList();

		return new PageResponse<>(
				items,
				studentPage.getNumber(),
				studentPage.getSize(),
				studentPage.getTotalElements(),
				studentPage.getTotalPages()
		);
	}

	@Override
	public StudentResponse getById(Long id) {
		return toResponse(findStudent(id));
	}

	@Override
	public StudentResponse update(Long id, StudentRequest request) {
		Student student = findStudent(id);
		updateStudentFields(student, request);
		return toResponse(studentRepository.save(student));
	}

	@Override
	public void delete(Long id) {
		Student student = findStudent(id);
		studentRepository.delete(student);
	}

	private Student findStudent(Long id) {
		return studentRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Student with id " + id + " was not found"));
	}

	private Pageable buildPageable(GridRequest request) {
		Sort sort = buildSort(request);
		if (request.pageSize() == null) {
			return Pageable.unpaged(sort);
		}

		return PageRequest.of(request.pageIndex(), request.pageSize(), sort);
	}

	private Specification<Student> buildSpecification(GridRequest request) {
		return (root, query, criteriaBuilder) -> {
			if (request.filters() == null || !StringUtils.hasText(request.filters().name())) {
				return criteriaBuilder.conjunction();
			}

			String name = "%" + request.filters().name().toLowerCase() + "%";
			return criteriaBuilder.or(
					criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), name),
					criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), name),
					criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), name)
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

	private void updateStudentFields(Student student, StudentRequest request) {
		student.setFirstName(request.firstName());
		student.setLastName(request.lastName());
		student.setEmail(request.email());
	}

	private StudentResponse toResponse(Student student) {
		return new StudentResponse(
				student.getId(),
				student.getFirstName(),
				student.getLastName(),
				student.getEmail()
		);
	}
}
