package com.spring_midterm.midterm;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
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
				.with(user("user").roles("USER"))
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
				.with(user("admin").roles("ADMIN", "USER"))
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
	void userCannotCreateStudent() throws Exception {
		mockMvc.perform(post("/api/students")
				.with(user("user").roles("USER"))
				.contentType(MediaType.APPLICATION_JSON)
				.content(json(Map.of(
					"firstName", "Dorothy",
					"lastName", "Vaughan",
					"email", "dorothy.vaughan@example.com"
				))))
			.andExpect(status().isForbidden());
	}

	@Test
	void apiRequiresAuthentication() throws Exception {
		mockMvc.perform(get("/api/students/1"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void homePageIsPublic() throws Exception {
		mockMvc.perform(get("/"))
			.andExpect(status().isOk());
	}

	@Test
	void profilePageRequiresAuthentication() throws Exception {
		mockMvc.perform(get("/profile"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void authenticatedUserCanViewProfilePage() throws Exception {
		mockMvc.perform(get("/profile")
				.with(user("user").roles("USER")))
			.andExpect(status().isOk());
	}

	@Test
	void userCannotViewAdminPage() throws Exception {
		mockMvc.perform(get("/admin")
				.with(user("user").roles("USER")))
			.andExpect(status().isFound())
			.andExpect(redirectedUrl("/access-denied"));
	}

	@Test
	void accessDeniedPageExplainsMissingAdminRole() throws Exception {
		mockMvc.perform(get("/access-denied")
				.with(user("user").roles("USER")))
			.andExpect(status().isForbidden())
			.andExpect(content().string(org.hamcrest.Matchers.containsString("Admin access required")));
	}

	@Test
	void adminCanViewAdminPage() throws Exception {
		mockMvc.perform(get("/admin")
				.with(user("admin").roles("ADMIN", "USER")))
			.andExpect(status().isOk());
	}

	@Test
	void userCannotDeleteStudent() throws Exception {
		Long studentId = createStudent("Alan", "Turing", "alan.turing@example.com");

		mockMvc.perform(delete("/api/students/{id}", studentId)
				.with(user("user").roles("USER")))
			.andExpect(status().isForbidden());
	}

	@Test
	void userCanCreateTaskForExistingStudent() throws Exception {
		Long studentId = createStudent("Mary", "Jackson", "mary.jackson@example.com");

		mockMvc.perform(post("/api/students/{studentId}/tasks", studentId)
				.with(user("user").roles("USER"))
				.contentType(MediaType.APPLICATION_JSON)
				.content(json(Map.of(
					"title", "Prepare DevOps report",
					"description", "Write deployment notes",
					"completed", false
				))))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.title").value("Prepare DevOps report"));
	}

	@Test
	void adminCanDeleteStudent() throws Exception {
		Long studentId = createStudent("Katherine", "Johnson", "katherine.johnson@example.com");

		mockMvc.perform(delete("/api/students/{id}", studentId)
				.with(user("admin").roles("ADMIN", "USER")))
			.andExpect(status().isNoContent());
	}

	private Long createStudent(String firstName, String lastName, String email) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/students")
				.with(user("admin").roles("ADMIN", "USER"))
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

	private Long idFrom(MvcResult result) throws Exception {
		JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
		return response.get("id").asLong();
	}

	private String json(Object value) throws Exception {
		return objectMapper.writeValueAsString(value);
	}
}
