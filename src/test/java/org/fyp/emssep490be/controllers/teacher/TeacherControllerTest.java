package org.fyp.emssep490be.controllers.teacher;

import org.fyp.emssep490be.dtos.teacher.TeacherProfileDTO;
import org.fyp.emssep490be.services.teacher.TeacherService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TeacherController.class)
class TeacherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TeacherService teacherService;


    @Test
    void getTeacherProfile_ValidId_ReturnsTeacherProfile() throws Exception {
        // Given
        TeacherProfileDTO teacherProfile = new TeacherProfileDTO(
            1L, 1L, "EMP001", "John Doe", "john.doe@example.com", 
            "0123456789", "ACTIVE", Collections.emptyList(), Collections.emptyList()
        );

        when(teacherService.getTeacherProfile(1L)).thenReturn(teacherProfile);

        // When & Then
        mockMvc.perform(get("/api/v1/teachers/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Teacher profile retrieved"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.employeeCode").value("EMP001"))
                .andExpect(jsonPath("$.data.fullName").value("John Doe"))
                .andExpect(jsonPath("$.data.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.data.phone").value("0123456789"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.skills").isArray())
                .andExpect(jsonPath("$.data.availability").isArray());
    }

    @Test
    void getTeacherProfile_InvalidId_ReturnsNotFound() throws Exception {
        // Given
        when(teacherService.getTeacherProfile(999L))
            .thenThrow(new org.fyp.emssep490be.exceptions.CustomException(
                org.fyp.emssep490be.exceptions.ErrorCode.TEACHER_NOT_FOUND));

        // When & Then
        mockMvc.perform(get("/api/v1/teachers/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTeacherProfile_ZeroId_ReturnsBadRequest() throws Exception {
        // Given
        when(teacherService.getTeacherProfile(0L))
            .thenThrow(new org.fyp.emssep490be.exceptions.CustomException(
                org.fyp.emssep490be.exceptions.ErrorCode.INVALID_INPUT));

        // When & Then
        mockMvc.perform(get("/api/v1/teachers/0")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
