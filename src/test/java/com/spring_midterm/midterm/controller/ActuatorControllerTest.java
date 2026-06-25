package com.spring_midterm.midterm.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ActuatorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void health_shouldBePublicAndReturnUp() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components.db.status").value("UP"))
                .andExpect(jsonPath("$.components.diskSpace.status").value("UP"));
    }

    @Test
    void info_shouldBePublicAndReturnAppInfo() throws Exception {
        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.application").value("midterm"))
                .andExpect(jsonPath("$.version").value("1.0.0"))
                .andExpect(jsonPath("$.contactEmail").value("support@midterm.app"));
    }

    @Test
    void metrics_shouldRequireAdmin() throws Exception {
        mockMvc.perform(get("/actuator/metrics")
                        .with(user("user").roles("USER")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/access-denied"));
    }

    @Test
    void metrics_shouldBeAccessibleByAdmin() throws Exception {
        mockMvc.perform(get("/actuator/metrics")
                        .with(user("admin").roles("ADMIN", "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['jvm.memory.heap.used']").isNumber())
                .andExpect(jsonPath("$['db.students.count']").isNumber());
    }

    @Test
    void metricsByName_shouldReturnMetric() throws Exception {
        mockMvc.perform(get("/actuator/metrics/db.students.count")
                        .with(user("admin").roles("ADMIN", "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("db.students.count"))
                .andExpect(jsonPath("$.value").isNumber());
    }

    @Test
    void metricsByName_unknown_shouldReturn404() throws Exception {
        mockMvc.perform(get("/actuator/metrics/unknown.metric")
                        .with(user("admin").roles("ADMIN", "USER")))
                .andExpect(status().isNotFound());
    }
}
