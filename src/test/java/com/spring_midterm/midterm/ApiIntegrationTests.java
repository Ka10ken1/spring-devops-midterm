package com.spring_midterm.midterm;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void createsStudentAndReturnsStudentGrid() throws Exception {
		createStudent("Ada", "Lovelace", "ada.lovelace@example.com");

		mockMvc.perform(post("/api/students/grid/paged")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json(Map.of(
					"page_index", 0,
					"page_size", 10,
					"filters", Map.of("name", "Ada"),
					"sorting", Map.of("direction", 1, "sortingname", "firstName")
				))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data[0].firstName").value("Ada"))
			.andExpect(jsonPath("$.page_size").value(10))
			.andExpect(jsonPath("$.totalElements").value(1));
	}

	@Test
	void duplicateStudentEmailReturnsConflict() throws Exception {
		createStudent("Grace", "Hopper", "grace.hopper@example.com");

		mockMvc.perform(post("/api/students")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json(Map.of(
					"firstName", "Grace",
					"lastName", "Murray",
					"email", "grace.hopper@example.com"
				))))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.message").value("Student email already exists"));
	}

	@Test
	void managesTasksUnderStudentAndNotesUnderTask() throws Exception {
		Long studentId = createStudent("Alan", "Turing", "alan.turing@example.com");
		Long taskId = createTask(studentId, "Read chapter 1");
		Long noteId = createNote(taskId, "Bring questions to class");

		mockMvc.perform(post("/api/students/{studentId}/tasks/grid/paged", studentId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(json(Map.of(
					"page_index", 0,
					"filters", Map.of("name", "chapter")
				))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data[0].id").value(taskId))
			.andExpect(jsonPath("$.data[0].studentId").value(studentId));

		mockMvc.perform(get("/api/tasks/{taskId}/notes/{noteId}", taskId, noteId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content").value("Bring questions to class"))
			.andExpect(jsonPath("$.taskId").value(taskId));

		mockMvc.perform(put("/api/students/{studentId}/tasks/{taskId}", studentId, taskId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(json(Map.of(
					"title", "Read chapter 2",
					"description", "Updated assignment",
					"completed", true
				))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.title").value("Read chapter 2"))
			.andExpect(jsonPath("$.completed").value(true));

		mockMvc.perform(delete("/api/tasks/{taskId}/notes/{noteId}", taskId, noteId))
			.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/tasks/{taskId}/notes/{noteId}", taskId, noteId))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.message").value("Note with id " + noteId + " was not found"));
	}

	private Long createStudent(String firstName, String lastName, String email) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/students")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json(Map.of(
						"firstName", firstName,
						"lastName", lastName,
						"email", email
				))))
			.andExpect(status().isCreated())
			.andReturn();

		return idFrom(result);
	}

	private Long createTask(Long studentId, String title) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/students/{studentId}/tasks", studentId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(json(Map.of(
						"title", title,
						"description", "Integration test task",
						"completed", false
				))))
			.andExpect(status().isCreated())
			.andReturn();

		return idFrom(result);
	}

	private Long createNote(Long taskId, String content) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/tasks/{taskId}/notes", taskId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(json(Map.of("content", content))))
			.andExpect(status().isCreated())
			.andReturn();

		return idFrom(result);
	}

	private Long idFrom(MvcResult result) throws Exception {
		JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
		return response.get("id").asLong();
	}

	private String json(Object value) throws Exception {
		return objectMapper.writeValueAsString(value);
	}
}
