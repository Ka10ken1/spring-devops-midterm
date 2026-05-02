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

	private Long idFrom(MvcResult result) throws Exception {
		JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
		return response.get("id").asLong();
	}

	private String json(Object value) throws Exception {
		return objectMapper.writeValueAsString(value);
	}
}
