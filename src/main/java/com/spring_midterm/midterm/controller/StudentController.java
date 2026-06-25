package com.spring_midterm.midterm.controller;

import com.spring_midterm.midterm.actuator.IMetricsService;
import com.spring_midterm.midterm.dto.GridRequest;
import com.spring_midterm.midterm.dto.PageResponse;
import com.spring_midterm.midterm.dto.StudentRequest;
import com.spring_midterm.midterm.dto.StudentResponse;
import com.spring_midterm.midterm.service.IStudentService;
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
@RequestMapping("/api/students")
public class StudentController {

	private final IStudentService studentService;
	private final IMetricsService metricsService;

	public StudentController(IStudentService studentService, IMetricsService metricsService) {
		this.studentService = studentService;
		this.metricsService = metricsService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Create a student", description = "Adds a new student record.")
	public StudentResponse createStudent(@Valid @RequestBody StudentRequest request) {
		log.info("API request: create student with email {}", request.email());
		metricsService.incrementCounter("api.student.create");
		return studentService.create(request);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update a student", description = "Updates an existing student record.")
	public StudentResponse updateStudent(@PathVariable Long id, @Valid @RequestBody StudentRequest request) {
		log.info("API request: update student with id {}", id);
		metricsService.incrementCounter("api.student.update");
		return studentService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Delete a student", description = "Deletes a student record by id.")
	public void deleteStudent(@PathVariable Long id) {
		log.info("API request: delete student with id {}", id);
		metricsService.incrementCounter("api.student.delete");
		studentService.delete(id);
	}

	@PostMapping("/grid/paged")
	@Operation(summary = "Get student grid", description = "Returns paginated students using filters and sorting.")
	public PageResponse<StudentResponse> getGrid(@Valid @RequestBody GridRequest request) {
		return studentService.getGrid(request);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get student by id", description = "Returns one student record by id.")
	public StudentResponse getStudentById(@PathVariable Long id) {
		return studentService.getById(id);
	}
}
