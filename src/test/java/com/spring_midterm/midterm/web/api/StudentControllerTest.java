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
import com.spring_midterm.midterm.dto.response.PageResponse;
import com.spring_midterm.midterm.dto.request.StudentRequest;
import com.spring_midterm.midterm.dto.response.StudentResponse;
import com.spring_midterm.midterm.exception.ResourceNotFoundException;
import com.spring_midterm.midterm.service.IStudentService;
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
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IStudentService studentService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Test
    void createStudent_asAdmin_shouldReturn201() throws Exception {
        StudentRequest request = new StudentRequest("Alice", "Foo", "alice@example.com");
        StudentResponse response = new StudentResponse(1L, "Alice", "Foo", "alice@example.com");
        when(studentService.create(any(StudentRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/students")
                        .with(user("admin").roles("ADMIN", "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.firstName").value("Alice"));
    }

    @Test
    void createStudent_asUser_shouldReturn403() throws Exception {
        StudentRequest request = new StudentRequest("Bob", "Bar", "bob@example.com");

        mockMvc.perform(post("/api/students")
                        .with(user("user").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateStudent_shouldReturn200() throws Exception {
        StudentRequest request = new StudentRequest("Updated", "Name", "updated@example.com");
        StudentResponse response = new StudentResponse(1L, "Updated", "Name", "updated@example.com");
        when(studentService.update(eq(1L), any(StudentRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/students/{id}", 1L)
                        .with(user("admin").roles("ADMIN", "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"));
    }

    @Test
    void deleteStudent_asAdmin_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/students/{id}", 1L)
                        .with(user("admin").roles("ADMIN", "USER")))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteStudent_asUser_shouldReturn403() throws Exception {
        mockMvc.perform(delete("/api/students/{id}", 1L)
                        .with(user("user").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void getStudentById_shouldReturn200() throws Exception {
        StudentResponse response = new StudentResponse(1L, "Charlie", "Baz", "charlie@example.com");
        when(studentService.getById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/students/{id}", 1L)
                        .with(user("user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Charlie"));
    }

    @Test
    void getStudentById_whenNotFound_shouldReturn404() throws Exception {
        when(studentService.getById(99L)).thenThrow(new ResourceNotFoundException("student", 99L));

        mockMvc.perform(get("/api/students/{id}", 99L)
                        .with(user("user").roles("USER")))
                .andExpect(status().isNotFound());
    }

    @Test
    void getGrid_shouldReturn200() throws Exception {
        PageResponse<StudentResponse> page = new PageResponse<>(List.of(), 0, 10, 0, 0);
        when(studentService.getGrid(any(GridRequest.class))).thenReturn(page);

        mockMvc.perform(post("/api/students/grid/paged")
                        .with(user("user").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new GridRequest(0, 10, null, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page_size").value(10));
    }
}
