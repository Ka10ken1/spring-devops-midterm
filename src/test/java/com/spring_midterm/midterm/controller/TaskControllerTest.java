package com.spring_midterm.midterm.controller;

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
import com.spring_midterm.midterm.dto.GridRequest;
import com.spring_midterm.midterm.dto.PageResponse;
import com.spring_midterm.midterm.dto.TaskRequest;
import com.spring_midterm.midterm.dto.TaskResponse;
import com.spring_midterm.midterm.service.ITaskService;
import java.time.LocalDate;
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
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ITaskService taskService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Test
    void createTask_shouldReturn201() throws Exception {
        TaskRequest request = new TaskRequest("New Task", "Desc", false, LocalDate.now());
        TaskResponse response = new TaskResponse(1L, "New Task", "Desc", false, LocalDate.now(), 1L, "John Doe");
        when(taskService.create(eq(1L), any(TaskRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/students/{studentId}/tasks", 1L)
                        .with(user("user").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("New Task"));
    }

    @Test
    void updateTask_shouldReturn200() throws Exception {
        TaskRequest request = new TaskRequest("Updated", "Desc", true, null);
        TaskResponse response = new TaskResponse(1L, "Updated", "Desc", true, null, 1L, "John Doe");
        when(taskService.update(eq(1L), eq(1L), any(TaskRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/students/{studentId}/tasks/{taskId}", 1L, 1L)
                        .with(user("user").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated"));
    }

    @Test
    void deleteTask_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/students/{studentId}/tasks/{taskId}", 1L, 1L)
                        .with(user("user").roles("USER")))
                .andExpect(status().isNoContent());
    }

    @Test
    void getTaskById_shouldReturn200() throws Exception {
        TaskResponse response = new TaskResponse(1L, "Test", "Desc", false, null, 1L, "John Doe");
        when(taskService.getById(1L, 1L)).thenReturn(response);

        mockMvc.perform(get("/api/students/{studentId}/tasks/{taskId}", 1L, 1L)
                        .with(user("user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test"));
    }

    @Test
    void getGrid_shouldReturn200() throws Exception {
        PageResponse<TaskResponse> page = new PageResponse<>(List.of(), 0, 10, 0, 0);
        when(taskService.getGrid(eq(1L), any(GridRequest.class))).thenReturn(page);

        mockMvc.perform(post("/api/students/{studentId}/tasks/grid/paged", 1L)
                        .with(user("user").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new GridRequest(0, 10, null, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page_size").value(10));
    }
}
