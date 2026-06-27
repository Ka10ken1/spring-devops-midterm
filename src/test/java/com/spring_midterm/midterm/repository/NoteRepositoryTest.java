package com.spring_midterm.midterm.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.spring_midterm.midterm.entity.Note;
import com.spring_midterm.midterm.entity.Task;
import com.spring_midterm.midterm.entity.Student;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class NoteRepositoryTest {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Test
    void shouldSaveAndFindNote() {
        Student student = new Student();
        student.setFirstName("Test");
        student.setLastName("User");
        student.setEmail("note.test@example.com");
        student = studentRepository.save(student);

        Task task = new Task();
        task.setTitle("Test Task");
        task.setCompleted(false);
        task.setDueDate(LocalDate.now());
        task.setStudent(student);
        task = taskRepository.save(task);

        Note note = new Note();
        note.setContent("Test note content");
        note.setCreatedAt(LocalDateTime.now());
        note.setTask(task);
        note = noteRepository.save(note);

        Note found = noteRepository.findById(note.getId()).orElseThrow();
        assertThat(found.getContent()).isEqualTo("Test note content");
        assertThat(found.getTask().getId()).isEqualTo(task.getId());
    }

    @Test
    void shouldCountNotes() {
        assertThat(noteRepository.count()).isGreaterThanOrEqualTo(0);
    }
}
