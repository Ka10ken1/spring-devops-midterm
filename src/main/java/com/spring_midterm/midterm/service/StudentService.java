package com.spring_midterm.midterm.service;

import com.spring_midterm.midterm.dto.GridRequest;
import com.spring_midterm.midterm.dto.PageResponse;
import com.spring_midterm.midterm.dto.StudentRequest;
import com.spring_midterm.midterm.dto.StudentResponse;
import com.spring_midterm.midterm.entity.Student;
import com.spring_midterm.midterm.helper.SpecificationHelper;
import com.spring_midterm.midterm.repository.IRepository;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
public class StudentService extends AbstractCrudService<Student, StudentRequest, StudentResponse> implements IStudentService {

	private static final Map<String, String> SORT_FIELDS = Map.of(
			"id", "id",
			"firstName", "firstName",
			"lastName", "lastName",
			"name", "firstName",
			"email", "email"
	);

	public StudentService(IRepository<Student> studentRepository) {
		super(studentRepository);
	}

	@Override
	@Transactional
	@PreAuthorize("hasRole('ADMIN')")
	public StudentResponse create(StudentRequest request) {
		Student student = createEntity();
		updateStudentFields(student, request);
		log.info("Created student: {} {}", student.getFirstName(), student.getLastName());
		return saveAndRespond(student);
	}

	@Override
	public StudentResponse getById(Long id) {
		return toResponse(findOrThrow(id));
	}

	@Override
	@Transactional
	public StudentResponse update(Long id, StudentRequest request) {
		Student student = findOrThrow(id);
		updateStudentFields(student, request);
		log.info("Updated student with id {}", id);
		return saveAndRespond(student);
	}

	@Override
	@Transactional
	@PreAuthorize("hasRole('ADMIN')")
	public void delete(Long id) {
		repository.delete(findOrThrow(id));
		log.info("Deleted student with id {}", id);
	}

	@Override
	public PageResponse<StudentResponse> getGrid(GridRequest request) {
		String name = request.filters() != null ? request.filters().name() : null;
		return findGrid(SpecificationHelper.anyLikeIgnoreCase(name, "firstName", "lastName", "email"), request);
	}

	@Override
	protected Student createEntity() {
		return new Student();
	}

	@Override
	protected StudentResponse toResponse(Student student) {
		return new StudentResponse(
				student.getId(),
				student.getFirstName(),
				student.getLastName(),
				student.getEmail()
		);
	}

	@Override
	protected Map<String, String> getSortFields() {
		return SORT_FIELDS;
	}

	@Override
	protected String getResourceName() {
		return "student";
	}

	private void updateStudentFields(Student student, StudentRequest request) {
		student.setFirstName(request.firstName());
		student.setLastName(request.lastName());
		student.setEmail(request.email());
	}
}
