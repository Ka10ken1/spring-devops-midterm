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
import com.spring_midterm.midterm.dto.request.NoteRequest;
import com.spring_midterm.midterm.dto.response.NoteResponse;
import com.spring_midterm.midterm.dto.response.PageResponse;
import com.spring_midterm.midterm.dto.request.SortingRequest;
import com.spring_midterm.midterm.entity.Note;
import com.spring_midterm.midterm.entity.Task;
import com.spring_midterm.midterm.exception.ResourceNotFoundException;
import com.spring_midterm.midterm.service.impl.NoteService;
import com.spring_midterm.midterm.repository.IRepository;
import java.time.LocalDateTime;
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
class NoteServiceTest {

    @Mock
    private IRepository<Note> noteRepository;

    @Mock
    private IRepository<Task> taskRepository;

    private NoteService noteService;

    @BeforeEach
    void setUp() {
        noteService = new NoteService(noteRepository, taskRepository);
    }

    private Task createTask(Long id) {
        Task task = new Task();
        task.setId(id);
        task.setTitle("Test Task");
        return task;
    }

    private Note createNote(Long id, Task task) {
        Note note = new Note();
        note.setId(id);
        note.setContent("Test content");
        note.setCreatedAt(LocalDateTime.now());
        note.setTask(task);
        return note;
    }

    @Test
    void create_shouldReturnNoteResponse() {
        Task task = createTask(1L);
        NoteRequest request = new NoteRequest("New note content");
        Note saved = createNote(1L, task);
        saved.setContent("New note content");

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(noteRepository.save(any(Note.class))).thenReturn(saved);

        NoteResponse response = noteService.create(1L, request);

        assertNotNull(response);
        assertEquals("New note content", response.content());
        assertEquals(1L, response.taskId());
        verify(noteRepository).save(any(Note.class));
    }

    @Test
    void create_whenTaskNotFound_shouldThrow() {
        NoteRequest request = new NoteRequest("Content");
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> noteService.create(99L, request));
        verify(noteRepository, never()).save(any());
    }

    @Test
    void getById_shouldReturnNoteResponse() {
        Task task = createTask(1L);
        Note note = createNote(1L, task);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(noteRepository.findById(1L)).thenReturn(Optional.of(note));

        NoteResponse response = noteService.getById(1L, 1L);

        assertEquals(1L, response.id());
        assertEquals("Test content", response.content());
    }

    @Test
    void getById_whenNoteNotFound_shouldThrow() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(createTask(1L)));
        when(noteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> noteService.getById(1L, 99L));
    }

    @Test
    void getById_whenNoteNotForTask_shouldThrow() {
        Task task = createTask(1L);
        Task other = createTask(2L);
        Note note = createNote(1L, other);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(noteRepository.findById(1L)).thenReturn(Optional.of(note));

        assertThrows(ResourceNotFoundException.class, () -> noteService.getById(1L, 1L));
    }

    @Test
    void getGrid_shouldReturnPagedResults() {
        Task task = createTask(1L);
        Note note = createNote(1L, task);

        GridRequest gridRequest = new GridRequest(0, 10, new GridFilterRequest("Test"), new SortingRequest(1, "createdAt"));
        PageImpl<Note> page = new PageImpl<>(List.of(note));

        when(noteRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        PageResponse<NoteResponse> result = noteService.getGrid(1L, gridRequest);

        assertEquals(1, result.totalElements());
        assertEquals("Test content", result.data().getFirst().content());
    }

    @Test
    void update_shouldReturnUpdatedNoteResponse() {
        Task task = createTask(1L);
        Note existing = createNote(1L, task);
        NoteRequest request = new NoteRequest("Updated content");
        Note updated = createNote(1L, task);
        updated.setContent("Updated content");

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(noteRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(noteRepository.save(any(Note.class))).thenReturn(updated);

        NoteResponse response = noteService.update(1L, 1L, request);

        assertEquals("Updated content", response.content());
        verify(noteRepository).save(any(Note.class));
    }

    @Test
    void delete_shouldRemoveNote() {
        Task task = createTask(1L);
        Note note = createNote(1L, task);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(noteRepository.findById(1L)).thenReturn(Optional.of(note));

        noteService.delete(1L, 1L);

        verify(noteRepository).delete((Note) any());
    }

    @Test
    void delete_whenNoteNotFound_shouldThrow() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(createTask(1L)));
        when(noteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> noteService.delete(1L, 99L));
        verify(noteRepository, never()).delete((Note) any());
    }
}
