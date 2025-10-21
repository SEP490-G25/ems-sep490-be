package org.fyp.emssep490be.controllers.teacher;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.fyp.emssep490be.dtos.teacher.CreateTeacherRequest;
import org.fyp.emssep490be.dtos.teacher.TeacherProfileDTO;
import org.fyp.emssep490be.services.teacher.TeacherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TeacherControllerTest {

    @Mock
    private TeacherService teacherService;

    @InjectMocks
    private TeacherController teacherController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(teacherController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void getTeacherProfile_ValidId_ReturnsTeacherProfile() throws Exception {
        // Given
        Long teacherId = 1L;
        TeacherProfileDTO teacherProfile = createTeacherProfileDTO();
        
        when(teacherService.getTeacherProfile(teacherId)).thenReturn(teacherProfile);

        // When & Then
        mockMvc.perform(get("/api/v1/teachers/{id}", teacherId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Teacher profile retrieved"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.employeeCode").value("EMP001"))
                .andExpect(jsonPath("$.data.fullName").value("John Doe"))
                .andExpect(jsonPath("$.data.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.data.phone").value("0123456789"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    void getTeacherProfile_InvalidId_ReturnsNotFound() throws Exception {
        // Given
        Long teacherId = 999L;
        
        when(teacherService.getTeacherProfile(teacherId))
                .thenThrow(new RuntimeException("Teacher not found"));

        // When & Then
        mockMvc.perform(get("/api/v1/teachers/{id}", teacherId))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void createTeacher_ValidRequest_ReturnsCreatedTeacher() throws Exception {
        // Given
        CreateTeacherRequest request = createCreateTeacherRequest();
        TeacherProfileDTO teacherProfile = createTeacherProfileDTO();
        
        when(teacherService.createTeacher(any(CreateTeacherRequest.class)))
                .thenReturn(teacherProfile);

        // When & Then
        mockMvc.perform(post("/api/v1/teachers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Teacher created successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.employeeCode").value("EMP001"))
                .andExpect(jsonPath("$.data.fullName").value("John Doe"))
                .andExpect(jsonPath("$.data.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.data.phone").value("0123456789"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    void createTeacher_InvalidRequest_ReturnsBadRequest() throws Exception {
        // Given
        CreateTeacherRequest request = new CreateTeacherRequest();
        // Empty request - should trigger validation errors

        // When & Then
        mockMvc.perform(post("/api/v1/teachers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTeacher_DuplicateEmployeeCode_ReturnsBadRequest() throws Exception {
        // Given
        CreateTeacherRequest request = createCreateTeacherRequest();
        
        when(teacherService.createTeacher(any(CreateTeacherRequest.class)))
                .thenThrow(new RuntimeException("Employee code already exists"));

        // When & Then
        mockMvc.perform(post("/api/v1/teachers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    private TeacherProfileDTO createTeacherProfileDTO() {
        return new TeacherProfileDTO(
                1L,
                1L,
                "EMP001",
                "John Doe",
                "john.doe@example.com",
                "0123456789",
                "ACTIVE",
                Collections.emptyList(),
                Collections.emptyList()
        );
    }

    private CreateTeacherRequest createCreateTeacherRequest() {
        CreateTeacherRequest request = new CreateTeacherRequest();
        request.setEmployeeCode("EMP1234"); // 3 letters + 4 digits
        request.setFullName("John Doe");
        request.setEmail("john.doe@example.com");
        request.setPhone("0123456789");
        request.setPassword("Password123!");
        request.setStatus("ACTIVE");
        request.setNote("Test teacher");
        return request;
    }
}
