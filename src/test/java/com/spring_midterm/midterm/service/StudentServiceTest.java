package com.spring_midterm.midterm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.spring_midterm.midterm.dto.request.GridFilterRequest;
import com.spring_midterm.midterm.dto.request.GridRequest;
import com.spring_midterm.midterm.dto.response.PageResponse;
import com.spring_midterm.midterm.dto.request.SortingRequest;
import com.spring_midterm.midterm.dto.request.StudentRequest;
import com.spring_midterm.midterm.dto.response.StudentResponse;
import com.spring_midterm.midterm.entity.Student;
import com.spring_midterm.midterm.exception.ResourceNotFoundException;
import com.spring_midterm.midterm.service.impl.StudentService;
import com.spring_midterm.midterm.repository.IRepository;
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
class StudentServiceTest {

    @Mock
    private IRepository<Student> studentRepository;

    private StudentService studentService;

    @BeforeEach
    void setUp() {
        studentService = new StudentService(studentRepository);
    }

    @Test
    void create_shouldReturnStudentResponse() {
        StudentRequest request = new StudentRequest("Alice", "Foo", "alice@example.com");
        Student saved = new Student();
        saved.setId(1L);
        saved.setFirstName("Alice");
        saved.setLastName("Foo");
        saved.setEmail("alice@example.com");
        when(studentRepository.save(any(Student.class))).thenReturn(saved);

        StudentResponse response = studentService.create(request);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Alice", response.firstName());
        assertEquals("Foo", response.lastName());
        assertEquals("alice@example.com", response.email());
        verify(studentRepository).save(any(Student.class));
    }

    @Test
    void getById_shouldReturnStudentResponse() {
        Student student = new Student();
        student.setId(1L);
        student.setFirstName("Bob");
        student.setLastName("Bar");
        student.setEmail("bob@example.com");
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

        StudentResponse response = studentService.getById(1L);

        assertEquals(1L, response.id());
        assertEquals("Bob", response.firstName());
        verify(studentRepository).findById(1L);
    }

    @Test
    void getById_whenNotFound_shouldThrow() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> studentService.getById(99L));
    }

    @Test
    void getGrid_shouldReturnPagedResults() {
        Student student = new Student();
        student.setId(1L);
        student.setFirstName("Charlie");
        student.setLastName("Baz");
        student.setEmail("charlie@example.com");

        GridRequest gridRequest = new GridRequest(0, 10, new GridFilterRequest("Charlie"), new SortingRequest(1, "firstName"));
        PageImpl<Student> page = new PageImpl<>(List.of(student));
        when(studentRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        PageResponse<StudentResponse> result = studentService.getGrid(gridRequest);

        assertEquals(1, result.totalElements());
        assertEquals("Charlie", result.data().getFirst().firstName());
    }

    @Test
    void getGrid_withoutPageSize_shouldReturnUnpaged() {
        Student student = new Student();
        student.setId(2L);
        student.setFirstName("Diana");
        student.setLastName("Qux");
        student.setEmail("diana@example.com");

        GridRequest gridRequest = new GridRequest(0, null, null, null);
        PageImpl<Student> page = new PageImpl<>(List.of(student));
        when(studentRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        PageResponse<StudentResponse> result = studentService.getGrid(gridRequest);

        assertEquals(1, result.data().size());
        assertEquals("Diana", result.data().getFirst().firstName());
    }

    @Test
    void update_shouldReturnUpdatedStudentResponse() {
        Student existing = new Student();
        existing.setId(1L);
        existing.setFirstName("Old");
        existing.setLastName("Name");
        existing.setEmail("old@example.com");

        StudentRequest request = new StudentRequest("New", "Name", "new@example.com");

        Student updated = new Student();
        updated.setId(1L);
        updated.setFirstName("New");
        updated.setLastName("Name");
        updated.setEmail("new@example.com");

        when(studentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(studentRepository.save(any(Student.class))).thenReturn(updated);

        StudentResponse response = studentService.update(1L, request);

        assertEquals("New", response.firstName());
        assertEquals("new@example.com", response.email());
        verify(studentRepository).save(any(Student.class));
    }

    @Test
    void update_whenNotFound_shouldThrow() {
        StudentRequest request = new StudentRequest("X", "Y", "x@example.com");
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> studentService.update(99L, request));
        verify(studentRepository, never()).save(any());
    }

    @Test
    void delete_shouldRemoveStudent() {
        Student student = new Student();
        student.setId(1L);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

        studentService.delete(1L);

        verify(studentRepository).delete((Student) any());
    }

    @Test
    void delete_whenNotFound_shouldThrow() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> studentService.delete(99L));
        verify(studentRepository, never()).delete((Student) any());
    }
}
