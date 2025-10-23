package org.fyp.emssep490be.controllers.studentrequest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.fyp.emssep490be.dtos.studentrequest.ApproveRequestDTO;
import org.fyp.emssep490be.dtos.studentrequest.CreateAbsenceRequestDTO;
import org.fyp.emssep490be.dtos.studentrequest.RejectRequestDTO;
import org.fyp.emssep490be.dtos.studentrequest.SessionBasicDTO;
import org.fyp.emssep490be.dtos.studentrequest.StudentRequestDTO;
import org.fyp.emssep490be.entities.enums.RequestStatus;
import org.fyp.emssep490be.entities.enums.SessionStatus;
import org.fyp.emssep490be.entities.enums.StudentRequestType;
import org.fyp.emssep490be.exceptions.CustomException;
import org.fyp.emssep490be.exceptions.ErrorCode;
import org.fyp.emssep490be.exceptions.GlobalExceptionHandler;
import org.fyp.emssep490be.services.studentrequest.StudentRequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for StudentRequestController
 * Tests all REST endpoints for absence request operations
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StudentRequestController Integration Tests")
class StudentRequestControllerTest {

    @Mock
    private StudentRequestService studentRequestService;

    @InjectMocks
    private StudentRequestController studentRequestController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private StudentRequestDTO testRequestDTO;
    private SessionBasicDTO testSessionDTO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(studentRequestController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Setup test session DTO
        testSessionDTO = SessionBasicDTO.builder()
                .id(100L)
                .date(LocalDate.now().plusDays(3))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(11, 30))
                .topic("Listening Practice")
                .className("English A1 Morning")
                .branchName("Test Branch")
                .status(SessionStatus.PLANNED)
                .sequenceNo(10)
                .build();

        // Setup test request DTO
        testRequestDTO = StudentRequestDTO.builder()
                .id(1L)
                .requestType(StudentRequestType.ABSENCE)
                .status(RequestStatus.PENDING)
                .studentId(1L)
                .studentName("Test Student")
                .studentEmail("student@test.com")
                .targetSessionId(100L)
                .targetSession(testSessionDTO)
                .reason("Family emergency - need to attend urgent matter")
                .submittedAt(LocalDateTime.now())
                .submittedBy(100L)
                .submittedByName("Test Student")
                .build();
    }

    @Nested
    @DisplayName("Student Operations - Create Absence Request")
    class CreateAbsenceRequestTests {

        @Test
        @DisplayName("POST /students/{id}/requests/absence - Should create absence request successfully")
        void createAbsenceRequest_ValidRequest_ReturnsCreated() throws Exception {
            // Given
            Long studentId = 1L;
            CreateAbsenceRequestDTO requestDTO = CreateAbsenceRequestDTO.builder()
                    .targetSessionId(100L)
                    .reason("Family emergency - need to attend urgent matter")
                    .build();

            when(studentRequestService.createAbsenceRequest(eq(studentId), any(CreateAbsenceRequestDTO.class)))
                    .thenReturn(testRequestDTO);

            // When & Then
            mockMvc.perform(post("/api/v1/students/{studentId}/requests/absence", studentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(201))
                    .andExpect(jsonPath("$.message").value("Absence request created successfully"))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.requestType").value("ABSENCE"))
                    .andExpect(jsonPath("$.data.status").value("PENDING"))
                    .andExpect(jsonPath("$.data.studentId").value(1))
                    .andExpect(jsonPath("$.data.targetSessionId").value(100));

            verify(studentRequestService, times(1)).createAbsenceRequest(eq(studentId), any(CreateAbsenceRequestDTO.class));
        }

        @Test
        @DisplayName("POST /students/{id}/requests/absence - Should return 400 when student not found")
        void createAbsenceRequest_StudentNotFound_ReturnsBadRequest() throws Exception {
            // Given
            Long studentId = 999L;
            CreateAbsenceRequestDTO requestDTO = CreateAbsenceRequestDTO.builder()
                    .targetSessionId(100L)
                    .reason("Family emergency")
                    .build();

            when(studentRequestService.createAbsenceRequest(eq(studentId), any(CreateAbsenceRequestDTO.class)))
                    .thenThrow(new CustomException(ErrorCode.STUDENT_NOT_FOUND));

            // When & Then
            mockMvc.perform(post("/api/v1/students/{studentId}/requests/absence", studentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(ErrorCode.STUDENT_NOT_FOUND.getCode())); // Returns error code, not HTTP status

            verify(studentRequestService, times(1)).createAbsenceRequest(eq(studentId), any(CreateAbsenceRequestDTO.class));
        }

        @Test
        @DisplayName("POST /students/{id}/requests/absence - Should return 400 when session not found")
        void createAbsenceRequest_SessionNotFound_ReturnsBadRequest() throws Exception {
            // Given
            Long studentId = 1L;
            CreateAbsenceRequestDTO requestDTO = CreateAbsenceRequestDTO.builder()
                    .targetSessionId(999L)
                    .reason("Family emergency")
                    .build();

            when(studentRequestService.createAbsenceRequest(eq(studentId), any(CreateAbsenceRequestDTO.class)))
                    .thenThrow(new CustomException(ErrorCode.SESSION_NOT_FOUND));

            // When & Then
            mockMvc.perform(post("/api/v1/students/{studentId}/requests/absence", studentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(studentRequestService, times(1)).createAbsenceRequest(eq(studentId), any(CreateAbsenceRequestDTO.class));
        }

        @Test
        @DisplayName("POST /students/{id}/requests/absence - Should return 400 when student not enrolled")
        void createAbsenceRequest_NotEnrolled_ReturnsBadRequest() throws Exception {
            // Given
            Long studentId = 1L;
            CreateAbsenceRequestDTO requestDTO = CreateAbsenceRequestDTO.builder()
                    .targetSessionId(100L)
                    .reason("Family emergency")
                    .build();

            when(studentRequestService.createAbsenceRequest(eq(studentId), any(CreateAbsenceRequestDTO.class)))
                    .thenThrow(new CustomException(ErrorCode.STUDENT_NOT_ENROLLED_IN_CLASS));

            // When & Then
            mockMvc.perform(post("/api/v1/students/{studentId}/requests/absence", studentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(studentRequestService, times(1)).createAbsenceRequest(eq(studentId), any(CreateAbsenceRequestDTO.class));
        }

        @Test
        @DisplayName("POST /students/{id}/requests/absence - Should return 400 when lead time not met")
        void createAbsenceRequest_LeadTimeNotMet_ReturnsBadRequest() throws Exception {
            // Given
            Long studentId = 1L;
            CreateAbsenceRequestDTO requestDTO = CreateAbsenceRequestDTO.builder()
                    .targetSessionId(100L)
                    .reason("Family emergency")
                    .build();

            when(studentRequestService.createAbsenceRequest(eq(studentId), any(CreateAbsenceRequestDTO.class)))
                    .thenThrow(new CustomException(ErrorCode.ABSENCE_REQUEST_LEAD_TIME_NOT_MET));

            // When & Then
            mockMvc.perform(post("/api/v1/students/{studentId}/requests/absence", studentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(studentRequestService, times(1)).createAbsenceRequest(eq(studentId), any(CreateAbsenceRequestDTO.class));
        }

        @Test
        @DisplayName("POST /students/{id}/requests/absence - Should return 400 for duplicate request")
        void createAbsenceRequest_DuplicateRequest_ReturnsBadRequest() throws Exception {
            // Given
            Long studentId = 1L;
            CreateAbsenceRequestDTO requestDTO = CreateAbsenceRequestDTO.builder()
                    .targetSessionId(100L)
                    .reason("Family emergency")
                    .build();

            when(studentRequestService.createAbsenceRequest(eq(studentId), any(CreateAbsenceRequestDTO.class)))
                    .thenThrow(new CustomException(ErrorCode.DUPLICATE_ABSENCE_REQUEST));

            // When & Then
            mockMvc.perform(post("/api/v1/students/{studentId}/requests/absence", studentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(studentRequestService, times(1)).createAbsenceRequest(eq(studentId), any(CreateAbsenceRequestDTO.class));
        }

        @Test
        @DisplayName("POST /students/{id}/requests/absence - Should return 400 for invalid request body")
        void createAbsenceRequest_InvalidBody_ReturnsBadRequest() throws Exception {
            // Given
            Long studentId = 1L;
            CreateAbsenceRequestDTO requestDTO = CreateAbsenceRequestDTO.builder()
                    .targetSessionId(null) // Invalid - required field
                    .reason("Too short") // Invalid - min 10 chars
                    .build();

            // When & Then
            mockMvc.perform(post("/api/v1/students/{studentId}/requests/absence", studentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(studentRequestService, never()).createAbsenceRequest(anyLong(), any());
        }
    }

    @Nested
    @DisplayName("Student Operations - Query Requests")
    class QueryRequestsTests {

        @Test
        @DisplayName("GET /students/{id}/requests - Should return student requests with filters")
        void getStudentRequests_ValidFilters_ReturnsRequests() throws Exception {
            // Given
            Long studentId = 1L;
            List<StudentRequestDTO> requests = Arrays.asList(testRequestDTO);

            when(studentRequestService.getStudentRequests(
                    eq(studentId), eq(StudentRequestType.ABSENCE), eq(RequestStatus.PENDING)))
                    .thenReturn(requests);

            // When & Then
            mockMvc.perform(get("/api/v1/students/{studentId}/requests", studentId)
                            .param("type", "ABSENCE")
                            .param("status", "PENDING"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("Requests retrieved successfully"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].id").value(1))
                    .andExpect(jsonPath("$.data[0].requestType").value("ABSENCE"));

            verify(studentRequestService, times(1)).getStudentRequests(studentId, StudentRequestType.ABSENCE, RequestStatus.PENDING);
        }

        @Test
        @DisplayName("GET /students/{id}/requests/{requestId} - Should return request details")
        void getRequestById_ValidId_ReturnsRequest() throws Exception {
            // Given
            Long studentId = 1L;
            Long requestId = 1L;

            when(studentRequestService.getRequestById(requestId)).thenReturn(testRequestDTO);

            // When & Then
            mockMvc.perform(get("/api/v1/students/{studentId}/requests/{requestId}", studentId, requestId))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.requestType").value("ABSENCE"));

            verify(studentRequestService, times(1)).getRequestById(requestId);
        }

        @Test
        @DisplayName("POST /students/{id}/requests/{requestId}/cancel - Should cancel request successfully")
        void cancelRequest_ValidRequest_ReturnsSuccess() throws Exception {
            // Given
            Long studentId = 1L;
            Long requestId = 1L;
            testRequestDTO.setStatus(RequestStatus.CANCELLED);

            when(studentRequestService.cancelRequest(requestId, studentId)).thenReturn(testRequestDTO);

            // When & Then
            mockMvc.perform(post("/api/v1/students/{studentId}/requests/{requestId}/cancel", studentId, requestId))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("Request cancelled successfully"))
                    .andExpect(jsonPath("$.data.status").value("CANCELLED"));

            verify(studentRequestService, times(1)).cancelRequest(requestId, studentId);
        }

        @Test
        @DisplayName("POST /students/{id}/requests/{requestId}/cancel - Should return 400 when not owner (CustomException always returns 400)")
        void cancelRequest_NotOwner_ReturnsBadRequest() throws Exception {
            // Given
            Long studentId = 1L;
            Long requestId = 1L;

            when(studentRequestService.cancelRequest(requestId, studentId))
                    .thenThrow(new CustomException(ErrorCode.FORBIDDEN));

            // When & Then
            mockMvc.perform(post("/api/v1/students/{studentId}/requests/{requestId}/cancel", studentId, requestId))
                    .andDo(print())
                    .andExpect(status().isBadRequest()) // GlobalExceptionHandler returns 400 for all CustomExceptions
                    .andExpect(jsonPath("$.status").value(ErrorCode.FORBIDDEN.getCode()));

            verify(studentRequestService, times(1)).cancelRequest(requestId, studentId);
        }
    }

    @Nested
    @DisplayName("Academic Staff Operations - Approve/Reject")
    class ApproveRejectRequestsTests {

        @Test
        @DisplayName("POST /student-requests/{id}/approve - Should approve request successfully")
        void approveRequest_ValidRequest_ReturnsSuccess() throws Exception {
            // Given
            Long requestId = 1L;
            ApproveRequestDTO approveDTO = ApproveRequestDTO.builder()
                    .decisionNotes("Approved. Valid reason provided.")
                    .build();

            testRequestDTO.setStatus(RequestStatus.APPROVED);
            testRequestDTO.setDecidedAt(LocalDateTime.now());
            testRequestDTO.setDecidedBy(200L);
            testRequestDTO.setDecidedByName("Test Staff");

            when(studentRequestService.approveAbsenceRequest(eq(requestId), anyLong(), any(ApproveRequestDTO.class)))
                    .thenReturn(testRequestDTO);

            // When & Then
            mockMvc.perform(post("/api/v1/student-requests/{requestId}/approve", requestId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(approveDTO)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("Request approved successfully"))
                    .andExpect(jsonPath("$.data.status").value("APPROVED"))
                    .andExpect(jsonPath("$.data.decidedBy").value(200));

            verify(studentRequestService, times(1)).approveAbsenceRequest(eq(requestId), anyLong(), any(ApproveRequestDTO.class));
        }

        @Test
        @DisplayName("POST /student-requests/{id}/approve - Should return 400 when request not found")
        void approveRequest_RequestNotFound_ReturnsBadRequest() throws Exception {
            // Given
            Long requestId = 999L;
            ApproveRequestDTO approveDTO = new ApproveRequestDTO();

            when(studentRequestService.approveAbsenceRequest(eq(requestId), anyLong(), any(ApproveRequestDTO.class)))
                    .thenThrow(new CustomException(ErrorCode.STUDENT_REQUEST_NOT_FOUND));

            // When & Then
            mockMvc.perform(post("/api/v1/student-requests/{requestId}/approve", requestId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(approveDTO)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(studentRequestService, times(1)).approveAbsenceRequest(eq(requestId), anyLong(), any(ApproveRequestDTO.class));
        }

        @Test
        @DisplayName("POST /student-requests/{id}/approve - Should return 400 when request not pending")
        void approveRequest_NotPending_ReturnsBadRequest() throws Exception {
            // Given
            Long requestId = 1L;
            ApproveRequestDTO approveDTO = new ApproveRequestDTO();

            when(studentRequestService.approveAbsenceRequest(eq(requestId), anyLong(), any(ApproveRequestDTO.class)))
                    .thenThrow(new CustomException(ErrorCode.REQUEST_NOT_PENDING));

            // When & Then
            mockMvc.perform(post("/api/v1/student-requests/{requestId}/approve", requestId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(approveDTO)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(studentRequestService, times(1)).approveAbsenceRequest(eq(requestId), anyLong(), any(ApproveRequestDTO.class));
        }

        @Test
        @DisplayName("POST /student-requests/{id}/reject - Should reject request successfully")
        void rejectRequest_ValidRequest_ReturnsSuccess() throws Exception {
            // Given
            Long requestId = 1L;
            RejectRequestDTO rejectDTO = RejectRequestDTO.builder()
                    .rejectionReason("Target class is full. Please choose another class.")
                    .build();

            testRequestDTO.setStatus(RequestStatus.REJECTED);
            testRequestDTO.setDecidedAt(LocalDateTime.now());

            when(studentRequestService.rejectAbsenceRequest(eq(requestId), anyLong(), any(RejectRequestDTO.class)))
                    .thenReturn(testRequestDTO);

            // When & Then
            mockMvc.perform(post("/api/v1/student-requests/{requestId}/reject", requestId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(rejectDTO)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("Request rejected"))
                    .andExpect(jsonPath("$.data.status").value("REJECTED"));

            verify(studentRequestService, times(1)).rejectAbsenceRequest(eq(requestId), anyLong(), any(RejectRequestDTO.class));
        }

        @Test
        @DisplayName("POST /student-requests/{id}/reject - Should return 400 for invalid rejection reason")
        void rejectRequest_InvalidReason_ReturnsBadRequest() throws Exception {
            // Given
            Long requestId = 1L;
            RejectRequestDTO rejectDTO = RejectRequestDTO.builder()
                    .rejectionReason("Short") // Too short - min 10 chars
                    .build();

            // When & Then
            mockMvc.perform(post("/api/v1/student-requests/{requestId}/reject", requestId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(rejectDTO)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(studentRequestService, never()).rejectAbsenceRequest(anyLong(), anyLong(), any());
        }
    }

    @Nested
    @DisplayName("Academic Staff Operations - Query All Requests")
    class QueryAllRequestsTests {

        @Test
        @DisplayName("GET /student-requests - Should return paginated requests with filters")
        void getAllRequests_ValidFilters_ReturnsPaginatedRequests() throws Exception {
            // Given
            Page<StudentRequestDTO> page = new PageImpl<>(Arrays.asList(testRequestDTO), PageRequest.of(0, 20), 1);

            when(studentRequestService.getAllRequests(
                    eq(RequestStatus.PENDING),
                    eq(StudentRequestType.ABSENCE),
                    isNull(),
                    isNull(),
                    any()))
                    .thenReturn(page);

            // When & Then
            mockMvc.perform(get("/api/v1/student-requests")
                            .param("status", "PENDING")
                            .param("type", "ABSENCE")
                            .param("page", "0")
                            .param("size", "20"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("Requests retrieved successfully"))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content[0].id").value(1))
                    .andExpect(jsonPath("$.data.totalElements").value(1));

            verify(studentRequestService, times(1)).getAllRequests(
                    eq(RequestStatus.PENDING),
                    eq(StudentRequestType.ABSENCE),
                    isNull(),
                    isNull(),
                    any());
        }

        @Test
        @DisplayName("GET /student-requests/{id} - Should return request details for staff")
        void getRequestByIdForStaff_ValidId_ReturnsRequest() throws Exception {
            // Given
            Long requestId = 1L;

            when(studentRequestService.getRequestById(requestId)).thenReturn(testRequestDTO);

            // When & Then
            mockMvc.perform(get("/api/v1/student-requests/{requestId}", requestId))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.requestType").value("ABSENCE"));

            verify(studentRequestService, times(1)).getRequestById(requestId);
        }
    }
}
