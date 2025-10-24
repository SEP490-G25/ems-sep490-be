package org.fyp.emssep490be.controllers.teacher;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.fyp.emssep490be.dtos.teacher.CreateTeacherRequestDTO;
import org.fyp.emssep490be.dtos.teacher.TeacherProfileDTO;
import org.fyp.emssep490be.dtos.teacher.TeacherSkillsResponseDTO;
import org.fyp.emssep490be.dtos.teacher.UpdateTeacherSkillsRequestDTO;
import org.fyp.emssep490be.exceptions.CustomException;
import org.fyp.emssep490be.exceptions.ErrorCode;
import org.fyp.emssep490be.exceptions.GlobalExceptionHandler;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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
        mockMvc = MockMvcBuilders.standaloneSetup(teacherController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
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
                .thenThrow(new org.fyp.emssep490be.exceptions.CustomException(org.fyp.emssep490be.exceptions.ErrorCode.TEACHER_NOT_FOUND));

        // When & Then
        mockMvc.perform(get("/api/v1/teachers/{id}", teacherId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTeacher_ValidRequest_ReturnsCreatedTeacher() throws Exception {
        // Given
        CreateTeacherRequestDTO request = createCreateTeacherRequest();
        TeacherProfileDTO teacherProfile = createTeacherProfileDTO();
        
        when(teacherService.createTeacher(any(CreateTeacherRequestDTO.class)))
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
        CreateTeacherRequestDTO request = new CreateTeacherRequestDTO();
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
        CreateTeacherRequestDTO request = createCreateTeacherRequest();
        
        when(teacherService.createTeacher(any(CreateTeacherRequestDTO.class)))
                .thenThrow(new org.fyp.emssep490be.exceptions.CustomException(org.fyp.emssep490be.exceptions.ErrorCode.TEACHER_EMPLOYEE_CODE_ALREADY_EXISTS));

        // When & Then
        mockMvc.perform(post("/api/v1/teachers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
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

    private CreateTeacherRequestDTO createCreateTeacherRequest() {
        CreateTeacherRequestDTO request = new CreateTeacherRequestDTO();
        request.setEmployeeCode("EMP1234"); // 3 letters + 4 digits
        request.setFullName("John Doe");
        request.setEmail("john.doe@example.com");
        request.setPhone("0123456789");
        request.setPassword("Password123!");
        request.setStatus("ACTIVE");
        request.setNote("Test teacher");
        return request;
    }

    // DELETE TEACHER TESTS

    @Test
    void deleteTeacher_ValidId_ReturnsOk() throws Exception {
        // Given
        Long teacherId = 1L;

        doNothing().when(teacherService).deleteTeacher(teacherId);

        // When & Then
        mockMvc.perform(delete("/api/v1/teachers/{id}", teacherId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Teacher deleted successfully"))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(teacherService).deleteTeacher(teacherId);
    }

    @Test
    void deleteTeacher_InvalidId_ReturnsBadRequest() throws Exception {
        // Given
        Long teacherId = 0L;

        doThrow(new CustomException(ErrorCode.INVALID_INPUT))
                .when(teacherService).deleteTeacher(teacherId);

        // When & Then
        mockMvc.perform(delete("/api/v1/teachers/{id}", teacherId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(ErrorCode.INVALID_INPUT.getCode()));

        verify(teacherService).deleteTeacher(teacherId);
    }

    @Test
    void deleteTeacher_TeacherNotFound_ReturnsBadRequest() throws Exception {
        // Given
        Long teacherId = 999L;

        doThrow(new CustomException(ErrorCode.TEACHER_NOT_FOUND))
                .when(teacherService).deleteTeacher(teacherId);

        // When & Then
        mockMvc.perform(delete("/api/v1/teachers/{id}", teacherId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(ErrorCode.TEACHER_NOT_FOUND.getCode()));

        verify(teacherService).deleteTeacher(teacherId);
    }

    // UPDATE TEACHER SKILLS TESTS

    @Test
    void updateTeacherSkills_ValidRequest_ReturnsOk() throws Exception {
        // Given
        Long teacherId = 1L;
        UpdateTeacherSkillsRequestDTO request = createUpdateTeacherSkillsRequest();
        TeacherSkillsResponseDTO response = createTeacherSkillsResponse();

        when(teacherService.updateTeacherSkills(teacherId, request)).thenReturn(response);

        // When & Then
        mockMvc.perform(put("/api/v1/teachers/{id}/skills", teacherId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Teacher skills updated successfully"))
                .andExpect(jsonPath("$.data.teacherId").value(1))
                .andExpect(jsonPath("$.data.skills").isArray())
                .andExpect(jsonPath("$.data.skills[0].skill").value("speaking"))
                .andExpect(jsonPath("$.data.skills[0].level").value(5))
                .andExpect(jsonPath("$.data.skills[1].skill").value("listening"))
                .andExpect(jsonPath("$.data.skills[1].level").value(4));

        verify(teacherService).updateTeacherSkills(teacherId, request);
    }

    @Test
    void updateTeacherSkills_InvalidId_ReturnsBadRequest() throws Exception {
        // Given
        Long teacherId = 0L;
        UpdateTeacherSkillsRequestDTO request = createUpdateTeacherSkillsRequest();

        doThrow(new CustomException(ErrorCode.INVALID_INPUT))
                .when(teacherService).updateTeacherSkills(teacherId, request);

        // When & Then
        mockMvc.perform(put("/api/v1/teachers/{id}/skills", teacherId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(ErrorCode.INVALID_INPUT.getCode()));

        verify(teacherService).updateTeacherSkills(teacherId, request);
    }

    @Test
    void updateTeacherSkills_TeacherNotFound_ReturnsBadRequest() throws Exception {
        // Given
        Long teacherId = 999L;
        UpdateTeacherSkillsRequestDTO request = createUpdateTeacherSkillsRequest();

        doThrow(new CustomException(ErrorCode.TEACHER_NOT_FOUND))
                .when(teacherService).updateTeacherSkills(teacherId, request);

        // When & Then
        mockMvc.perform(put("/api/v1/teachers/{id}/skills", teacherId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(ErrorCode.TEACHER_NOT_FOUND.getCode()));

        verify(teacherService).updateTeacherSkills(teacherId, request);
    }

    @Test
    void updateTeacherSkills_InvalidRequest_ReturnsBadRequest() throws Exception {
        // Given
        Long teacherId = 1L;
        UpdateTeacherSkillsRequestDTO request = new UpdateTeacherSkillsRequestDTO();
        request.setSkills(Collections.emptyList()); // Invalid: empty skills

        // When & Then
        mockMvc.perform(put("/api/v1/teachers/{id}/skills", teacherId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        // Service should not be called due to validation failure
        verify(teacherService, never()).updateTeacherSkills(any(), any());
    }

    private UpdateTeacherSkillsRequestDTO createUpdateTeacherSkillsRequest() {
        UpdateTeacherSkillsRequestDTO request = new UpdateTeacherSkillsRequestDTO();
        
        UpdateTeacherSkillsRequestDTO.TeacherSkillDTO skill1 = new UpdateTeacherSkillsRequestDTO.TeacherSkillDTO();
        skill1.setSkill("speaking");
        skill1.setLevel(5);
        
        UpdateTeacherSkillsRequestDTO.TeacherSkillDTO skill2 = new UpdateTeacherSkillsRequestDTO.TeacherSkillDTO();
        skill2.setSkill("listening");
        skill2.setLevel(4);
        
        request.setSkills(java.util.List.of(skill1, skill2));
        return request;
    }

    private TeacherSkillsResponseDTO createTeacherSkillsResponse() {
        TeacherSkillsResponseDTO response = new TeacherSkillsResponseDTO();
        response.setTeacherId(1L);
        
        TeacherSkillsResponseDTO.TeacherSkillDTO skill1 = new TeacherSkillsResponseDTO.TeacherSkillDTO();
        skill1.setSkill("speaking");
        skill1.setLevel(5);
        
        TeacherSkillsResponseDTO.TeacherSkillDTO skill2 = new TeacherSkillsResponseDTO.TeacherSkillDTO();
        skill2.setSkill("listening");
        skill2.setLevel(4);
        
        response.setSkills(java.util.List.of(skill1, skill2));
        return response;
    }
}
