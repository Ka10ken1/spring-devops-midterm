package com.spring_midterm.midterm.controller;

import com.spring_midterm.midterm.dto.GridFilterRequest;
import com.spring_midterm.midterm.dto.GridRequest;
import com.spring_midterm.midterm.dto.NoteRequest;
import com.spring_midterm.midterm.dto.NoteResponse;
import com.spring_midterm.midterm.dto.PageResponse;
import com.spring_midterm.midterm.dto.SortingRequest;
import com.spring_midterm.midterm.dto.StudentRequest;
import com.spring_midterm.midterm.dto.StudentResponse;
import com.spring_midterm.midterm.dto.TaskRequest;
import com.spring_midterm.midterm.dto.TaskResponse;
import com.spring_midterm.midterm.service.INoteService;
import com.spring_midterm.midterm.service.IService;
import com.spring_midterm.midterm.service.ITaskService;
import java.time.LocalDate;
import java.security.Principal;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
public class PageController {

	private final IService<StudentRequest, StudentResponse> studentService;
	private final ITaskService taskService;
	private final INoteService noteService;

	public PageController(
			IService<StudentRequest, StudentResponse> studentService,
			ITaskService taskService,
			INoteService noteService
	) {
		this.studentService = studentService;
		this.taskService = taskService;
		this.noteService = noteService;
	}

	@GetMapping("/")
	public String home() {
		return "home";
	}

	@GetMapping("/login")
	public String login() {
		return "login";
	}

	@GetMapping("/profile")
	public String profile(Model model, Principal principal) {
		PageResponse<StudentResponse> students = studentService.getGrid(grid(null, "firstName"));
		model.addAttribute("username", principal.getName());
		model.addAttribute("students", students.data());
		model.addAttribute("studentCount", students.totalElements());
		return "profile";
	}

	@GetMapping("/access-denied")
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public String accessDenied() {
		return "access-denied";
	}

	@GetMapping("/students")
	public String students(@RequestParam(required = false) String search, Model model) {
		model.addAttribute("students", studentService.getGrid(grid(search, "firstName")).data());
		model.addAttribute("search", search);
		return "students";
	}

	@PostMapping("/admin/students")
	@PreAuthorize("hasRole('ADMIN')")
	public String createStudent(
			@RequestParam String firstName,
			@RequestParam String lastName,
			@RequestParam String email
	) {
		studentService.create(new StudentRequest(firstName, lastName, email));
		return "redirect:/admin";
	}

	@GetMapping("/students/{studentId}")
	public String studentDetails(@PathVariable Long studentId, Model model) {
		model.addAttribute("student", studentService.getById(studentId));
		model.addAttribute("tasks", taskService.getGrid(studentId, grid(null, "dueDate")).data());
		return "student-detail";
	}

	@PostMapping("/students/{studentId}/tasks")
	public String createTask(
			@PathVariable Long studentId,
			@RequestParam String title,
			@RequestParam(required = false) String description,
			@RequestParam(defaultValue = "false") boolean completed,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate
	) {
		taskService.create(studentId, new TaskRequest(title, description, completed, dueDate));
		return "redirect:/students/" + studentId;
	}

	@GetMapping("/students/{studentId}/tasks/{taskId}")
	public String taskDetails(@PathVariable Long studentId, @PathVariable Long taskId, Model model) {
		TaskResponse task = taskService.getById(studentId, taskId);
		PageResponse<NoteResponse> notes = noteService.getGrid(taskId, grid(null, "createdAt"));
		model.addAttribute("task", task);
		model.addAttribute("notes", notes.data());
		return "task-detail";
	}

	@PostMapping("/students/{studentId}/tasks/{taskId}/notes")
	public String createNote(@PathVariable Long studentId, @PathVariable Long taskId, @RequestParam String content) {
		noteService.create(taskId, new NoteRequest(content));
		return "redirect:/students/" + studentId + "/tasks/" + taskId;
	}

	@GetMapping("/admin")
	@PreAuthorize("hasRole('ADMIN')")
	public String admin(Model model) {
		model.addAttribute("students", studentService.getGrid(grid(null, "firstName")).data());
		return "admin";
	}

	@PostMapping("/admin/students/{studentId}/delete")
	@PreAuthorize("hasRole('ADMIN')")
	public String deleteStudent(@PathVariable Long studentId) {
		studentService.delete(studentId);
		return "redirect:/admin";
	}

	private GridRequest grid(String search, String sortField) {
		GridFilterRequest filters = search == null || search.isBlank() ? null : new GridFilterRequest(search);
		return new GridRequest(0, null, filters, new SortingRequest(1, sortField));
	}
}
