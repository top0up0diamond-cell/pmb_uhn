package com.uhn.pmb.controller;

import com.uhn.pmb.service.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class StudentControllerTest {

    @Mock
    private StudentService studentService;

    @InjectMocks
    private StudentController studentController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(studentController).build();
    }

    @Test
    @DisplayName("GET /api/admin/students - returns 200 with student list")
    void getAllStudents_returns200() throws Exception {
        when(studentService.getAllStudents()).thenReturn(
                Arrays.asList(Map.of("id", 1L, "fullName", "Alice")));

        mockMvc.perform(get("/api/students/all"))
                .andExpect(status().isOk());
        verify(studentService).getAllStudents();
    }

    @Test
    @DisplayName("GET /api/admin/students - empty list returns 200")
    void getAllStudents_emptyList_returns200() throws Exception {
        when(studentService.getAllStudents()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/students/all"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/students/{id} - found returns 200")
    void getStudentById_found_returns200() throws Exception {
        when(studentService.getStudentById(1L)).thenReturn(Map.of("id", 1L, "fullName", "Alice"));

        mockMvc.perform(get("/api/students/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/students/{id} - not found returns 404")
    void getStudentById_notFound_returns404() throws Exception {
        when(studentService.getStudentById(999L)).thenThrow(new RuntimeException("Student not found"));

        mockMvc.perform(get("/api/students/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/students/all - exception returns 400")
    void getAllStudents_exception_returns400() throws Exception {
        when(studentService.getAllStudents()).thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(get("/api/students/all"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/students/{id} - general exception returns 400")
    void getStudentById_generalException_returns400() throws Exception {
        // Use a Mockito spy to force general Exception path via direct invocation
        StudentService brokenService = mock(StudentService.class, invocation -> {
            if (invocation.getMethod().getName().equals("getStudentById")) {
                throw new java.io.IOException("Simulated checked exception");
            }
            return null;
        });
        StudentController ctrl = new StudentController(brokenService);

        ResponseEntity<?> response = ctrl.getStudentById(2L);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
    }
}
