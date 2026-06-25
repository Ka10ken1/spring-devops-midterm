package com.spring_midterm.midterm.web.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.spring_midterm.midterm.dto.request.GridRequest;
import com.spring_midterm.midterm.dto.request.NoteRequest;
import com.spring_midterm.midterm.dto.response.NoteResponse;
import com.spring_midterm.midterm.dto.response.PageResponse;
import com.spring_midterm.midterm.service.INoteService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private INoteService noteService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Test
    void createNote_shouldReturn201() throws Exception {
        NoteRequest request = new NoteRequest("New note");
        NoteResponse response = new NoteResponse(1L, "New note", LocalDateTime.now(), 1L, "Task Title");
        when(noteService.create(eq(1L), any(NoteRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/tasks/{taskId}/notes", 1L)
                        .with(user("user").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("New note"));
    }

    @Test
    void updateNote_shouldReturn200() throws Exception {
        NoteRequest request = new NoteRequest("Updated note");
        NoteResponse response = new NoteResponse(1L, "Updated note", LocalDateTime.now(), 1L, "Task Title");
        when(noteService.update(eq(1L), eq(1L), any(NoteRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/tasks/{taskId}/notes/{noteId}", 1L, 1L)
                        .with(user("user").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Updated note"));
    }

    @Test
    void deleteNote_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/tasks/{taskId}/notes/{noteId}", 1L, 1L)
                        .with(user("user").roles("USER")))
                .andExpect(status().isNoContent());
    }

    @Test
    void getNoteById_shouldReturn200() throws Exception {
        NoteResponse response = new NoteResponse(1L, "Note content", LocalDateTime.now(), 1L, "Task Title");
        when(noteService.getById(1L, 1L)).thenReturn(response);

        mockMvc.perform(get("/api/tasks/{taskId}/notes/{noteId}", 1L, 1L)
                        .with(user("user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Note content"));
    }

    @Test
    void getGrid_shouldReturn200() throws Exception {
        PageResponse<NoteResponse> page = new PageResponse<>(List.of(), 0, 10, 0, 0);
        when(noteService.getGrid(eq(1L), any(GridRequest.class))).thenReturn(page);

        mockMvc.perform(post("/api/tasks/{taskId}/notes/grid/paged", 1L)
                        .with(user("user").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new GridRequest(0, 10, null, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page_size").value(10));
    }
}
