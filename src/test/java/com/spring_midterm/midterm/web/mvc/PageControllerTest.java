package com.spring_midterm.midterm.web.mvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.spring_midterm.midterm.dto.request.GridRequest;
import com.spring_midterm.midterm.dto.request.NoteRequest;
import com.spring_midterm.midterm.dto.request.StudentRequest;
import com.spring_midterm.midterm.dto.request.TaskRequest;
import com.spring_midterm.midterm.dto.response.NoteResponse;
import com.spring_midterm.midterm.dto.response.PageResponse;
import com.spring_midterm.midterm.dto.response.StudentResponse;
import com.spring_midterm.midterm.dto.response.TaskResponse;
import com.spring_midterm.midterm.service.INoteService;
import com.spring_midterm.midterm.service.IStudentService;
import com.spring_midterm.midterm.service.ITaskService;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IStudentService studentService;

    @MockitoBean
    private ITaskService taskService;

    @MockitoBean
    private INoteService noteService;

    @Test
    void home_shouldReturnHomeView() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"));
    }

    @Test
    void login_shouldReturnLoginView() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    void profile_shouldReturnProfileView() throws Exception {
        var students = List.of(new StudentResponse(1L, "Alice", "Foo", "alice@example.com"));
        when(studentService.getGrid(any(GridRequest.class)))
                .thenReturn(new PageResponse<>(students, 0, 10, 1, 1));

        mockMvc.perform(get("/profile")
                        .with(user("user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("username", "students", "studentCount"));
    }

    @Test
    void profile_whenUnauthenticated_shouldReturn401() throws Exception {
        mockMvc.perform(get("/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void accessDenied_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/access-denied")
                        .with(user("user").roles("USER")))
                .andExpect(status().isForbidden())
                .andExpect(view().name("access-denied"));
    }

    @Test
    void students_withoutSearch_shouldReturnStudentsView() throws Exception {
        var students = List.of(new StudentResponse(1L, "Bob", "Bar", "bob@example.com"));
        when(studentService.getGrid(any(GridRequest.class)))
                .thenReturn(new PageResponse<>(students, 0, 10, 1, 1));

        mockMvc.perform(get("/students")
                        .with(user("user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("students"))
                .andExpect(model().attributeExists("students"));
    }

    @Test
    void students_withSearch_shouldReturnStudentsView() throws Exception {
        var students = List.of(new StudentResponse(2L, "Charlie", "Baz", "charlie@example.com"));
        when(studentService.getGrid(any(GridRequest.class)))
                .thenReturn(new PageResponse<>(students, 0, 10, 1, 1));

        mockMvc.perform(get("/students")
                        .param("search", "Charlie")
                        .with(user("user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("students"))
                .andExpect(model().attribute("search", "Charlie"));
    }

    @Test
    void createStudent_asAdmin_shouldRedirect() throws Exception {
        mockMvc.perform(post("/admin/students")
                        .param("firstName", "Diana")
                        .param("lastName", "Qux")
                        .param("email", "diana@example.com")
                        .with(user("admin").roles("ADMIN", "USER"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));

        verify(studentService).create(any(StudentRequest.class));
    }

    @Test
    void createStudent_asUser_shouldRedirectToAccessDenied() throws Exception {
        mockMvc.perform(post("/admin/students")
                        .param("firstName", "Eve")
                        .param("lastName", "Smith")
                        .param("email", "eve@example.com")
                        .with(user("user").roles("USER"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/access-denied"));
    }

    @Test
    void studentDetails_shouldReturnStudentDetailView() throws Exception {
        var student = new StudentResponse(1L, "Frank", "Jones", "frank@example.com");
        when(studentService.getById(1L)).thenReturn(student);
        when(taskService.getGrid(anyLong(), any(GridRequest.class)))
                .thenReturn(new PageResponse<>(List.of(), 0, 10, 0, 0));

        mockMvc.perform(get("/students/1")
                        .with(user("user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("student-detail"))
                .andExpect(model().attributeExists("student", "tasks"));
    }

    @Test
    void createTask_shouldRedirect() throws Exception {
        mockMvc.perform(post("/students/1/tasks")
                        .param("title", "Test task")
                        .param("description", "A description")
                        .param("completed", "false")
                        .param("dueDate", "2026-12-31")
                        .with(user("user").roles("USER"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/students/1"));

        verify(taskService).create(anyLong(), any(TaskRequest.class));
    }

    @Test
    void taskDetails_shouldReturnTaskDetailView() throws Exception {
        var task = new TaskResponse(1L, "Task Title", "Desc", false, LocalDate.now(), 1L, "Frank Jones");
        when(taskService.getById(1L, 1L)).thenReturn(task);
        when(noteService.getGrid(anyLong(), any(GridRequest.class)))
                .thenReturn(new PageResponse<>(List.of(), 0, 10, 0, 0));

        mockMvc.perform(get("/students/1/tasks/1")
                        .with(user("user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("task-detail"))
                .andExpect(model().attributeExists("task", "notes"));
    }

    @Test
    void createNote_shouldRedirect() throws Exception {
        mockMvc.perform(post("/students/1/tasks/1/notes")
                        .param("content", "A note about the task")
                        .with(user("user").roles("USER"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/students/1/tasks/1"));

        verify(noteService).create(anyLong(), any(NoteRequest.class));
    }

    @Test
    void admin_asAdmin_shouldReturnAdminView() throws Exception {
        var students = List.of(new StudentResponse(1L, "Grace", "Hopper", "grace@example.com"));
        when(studentService.getGrid(any(GridRequest.class)))
                .thenReturn(new PageResponse<>(students, 0, 10, 1, 1));

        mockMvc.perform(get("/admin")
                        .with(user("admin").roles("ADMIN", "USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("admin"))
                .andExpect(model().attributeExists("students"));
    }

    @Test
    void admin_asUser_shouldRedirectToAccessDenied() throws Exception {
        mockMvc.perform(get("/admin")
                        .with(user("user").roles("USER")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/access-denied"));
    }

    @Test
    void deleteStudent_asAdmin_shouldRedirect() throws Exception {
        mockMvc.perform(post("/admin/students/1/delete")
                        .with(user("admin").roles("ADMIN", "USER"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));

        verify(studentService).delete(1L);
    }

    @Test
    void deleteStudent_asUser_shouldRedirectToAccessDenied() throws Exception {
        mockMvc.perform(post("/admin/students/1/delete")
                        .with(user("user").roles("USER"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/access-denied"));
    }
}
