package com.spring_midterm.midterm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.spring_midterm.midterm.dto.request.GridFilterRequest;
import com.spring_midterm.midterm.dto.request.GridRequest;
import com.spring_midterm.midterm.dto.response.PageResponse;
import com.spring_midterm.midterm.dto.request.SortingRequest;
import com.spring_midterm.midterm.dto.request.TaskRequest;
import com.spring_midterm.midterm.dto.response.TaskResponse;
import com.spring_midterm.midterm.entity.Student;
import com.spring_midterm.midterm.entity.Task;
import com.spring_midterm.midterm.exception.ResourceNotFoundException;
import com.spring_midterm.midterm.service.impl.TaskService;
import com.spring_midterm.midterm.repository.IRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private IRepository<Task> taskRepository;

    @Mock
    private IRepository<Student> studentRepository;

    private TaskService taskService;

    @BeforeEach
    void setUp() {
        taskService = new TaskService(taskRepository, studentRepository);
    }

    private Student createStudent(Long id) {
        Student student = new Student();
        student.setId(id);
        student.setFirstName("John");
        student.setLastName("Doe");
        student.setEmail("john@example.com");
        return student;
    }

    private Task createTask(Long id, Student student) {
        Task task = new Task();
        task.setId(id);
        task.setTitle("Test Task");
        task.setDescription("A description");
        task.setCompleted(false);
        task.setDueDate(LocalDate.now());
        task.setStudent(student);
        return task;
    }

    @Test
    void create_shouldReturnTaskResponse() {
        Student student = createStudent(1L);
        TaskRequest request = new TaskRequest("New Task", "Desc", false, LocalDate.now());
        Task saved = createTask(1L, student);
        saved.setTitle("New Task");

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(taskRepository.save(any(Task.class))).thenReturn(saved);

        TaskResponse response = taskService.create(1L, request);

        assertNotNull(response);
        assertEquals("New Task", response.title());
        assertEquals(1L, response.studentId());
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void create_whenStudentNotFound_shouldThrow() {
        TaskRequest request = new TaskRequest("X", "Y", false, null);
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskService.create(99L, request));
        verify(taskRepository, never()).save(any());
    }

    @Test
    void getById_shouldReturnTaskResponse() {
        Student student = createStudent(1L);
        Task task = createTask(1L, student);

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        TaskResponse response = taskService.getById(1L, 1L);

        assertEquals(1L, response.id());
        assertEquals("Test Task", response.title());
    }

    @Test
    void getById_whenTaskNotFound_shouldThrow() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(createStudent(1L)));
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskService.getById(1L, 99L));
    }

    @Test
    void getById_whenTaskNotForStudent_shouldThrow() {
        Student student = createStudent(1L);
        Student other = createStudent(2L);
        Task task = createTask(1L, other);

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        assertThrows(ResourceNotFoundException.class, () -> taskService.getById(1L, 1L));
    }

    @Test
    void getGrid_shouldReturnPagedResults() {
        Student student = createStudent(1L);
        Task task = createTask(1L, student);

        GridRequest gridRequest = new GridRequest(0, 10, new GridFilterRequest("Test"), new SortingRequest(1, "title"));
        PageImpl<Task> page = new PageImpl<>(List.of(task));

        when(taskRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        PageResponse<TaskResponse> result = taskService.getGrid(1L, gridRequest);

        assertEquals(1, result.totalElements());
        assertEquals("Test Task", result.data().getFirst().title());
    }

    @Test
    void update_shouldReturnUpdatedTaskResponse() {
        Student student = createStudent(1L);
        Task existing = createTask(1L, student);
        TaskRequest request = new TaskRequest("Updated Title", "Updated Desc", true, LocalDate.now());
        Task updated = createTask(1L, student);
        updated.setTitle("Updated Title");
        updated.setCompleted(true);

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(taskRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(taskRepository.save(any(Task.class))).thenReturn(updated);

        TaskResponse response = taskService.update(1L, 1L, request);

        assertEquals("Updated Title", response.title());
        assertEquals(true, response.completed());
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void delete_shouldRemoveTask() {
        Student student = createStudent(1L);
        Task task = createTask(1L, student);

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        taskService.delete(1L, 1L);

        verify(taskRepository).delete((Task) any());
    }

    @Test
    void delete_whenTaskNotFound_shouldThrow() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(createStudent(1L)));
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskService.delete(1L, 99L));
        verify(taskRepository, never()).delete((Task) any());
    }
}
