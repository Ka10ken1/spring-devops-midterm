package com.spring_midterm.midterm.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.spring_midterm.midterm.entity.Student;
import com.spring_midterm.midterm.entity.Task;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class StudentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private IRepository<Student> studentRepository;

    @Test
    void saveAndFindById_shouldWork() {
        Student student = new Student();
        student.setFirstName("John");
        student.setLastName("Doe");
        student.setEmail("john.doe@example.com");

        Student saved = entityManager.persistAndFlush(student);

        Optional<Student> found = studentRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("John", found.get().getFirstName());
        assertEquals("john.doe@example.com", found.get().getEmail());
    }

    @Test
    void saveWithTasks_shouldCascade() {
        Student student = new Student();
        student.setFirstName("Jane");
        student.setLastName("Smith");
        student.setEmail("jane.smith@example.com");

        Task task = new Task();
        task.setTitle("Test task");
        task.setDescription("A task description");
        task.setCompleted(false);
        task.setDueDate(LocalDate.now());
        task.setStudent(student);

        student.setTasks(List.of(task));

        Student saved = entityManager.persistAndFlush(student);
        entityManager.clear();

        Student found = entityManager.find(Student.class, saved.getId());
        assertNotNull(found);
        assertEquals(1, found.getTasks().size());
        assertEquals("Test task", found.getTasks().getFirst().getTitle());
    }

    @Test
    void deleteStudent_shouldRemoveStudent() {
        Student student = new Student();
        student.setFirstName("Bob");
        student.setLastName("Bar");
        student.setEmail("bob.bar@example.com");

        Student saved = entityManager.persistAndFlush(student);
        studentRepository.deleteById(saved.getId());

        Optional<Student> found = studentRepository.findById(saved.getId());
        assertTrue(found.isEmpty());
    }
}
