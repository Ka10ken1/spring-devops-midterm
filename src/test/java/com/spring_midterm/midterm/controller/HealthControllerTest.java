package com.spring_midterm.midterm.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.spring_midterm.midterm.config.AppSettings;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(HealthController.class)
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AppSettings appSettings;

    @Test
    void health_shouldReturnUpStatus() throws Exception {
        when(appSettings.getTitle()).thenReturn("Midterm Application");
        when(appSettings.getPaginationLimit()).thenReturn(20);
        when(appSettings.getContactEmail()).thenReturn("support@midterm.app");

        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.application").value("midterm"))
                .andExpect(jsonPath("$.title").value("Midterm Application"))
                .andExpect(jsonPath("$.contactEmail").value("support@midterm.app"))
                .andExpect(jsonPath("$.paginationLimit").value(20))
                .andExpect(jsonPath("$.timestamp").isString());
    }
}
