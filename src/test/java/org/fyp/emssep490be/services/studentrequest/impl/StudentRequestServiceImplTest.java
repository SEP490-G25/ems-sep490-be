package org.fyp.emssep490be.services.studentrequest.impl;

import org.fyp.emssep490be.dtos.studentrequest.ApproveRequestDTO;
import org.fyp.emssep490be.dtos.studentrequest.CreateAbsenceRequestDTO;
import org.fyp.emssep490be.dtos.studentrequest.RejectRequestDTO;
import org.fyp.emssep490be.dtos.studentrequest.StudentRequestDTO;
import org.fyp.emssep490be.entities.*;
import org.fyp.emssep490be.entities.enums.*;
import org.fyp.emssep490be.entities.ids.StudentSessionId;
import org.fyp.emssep490be.exceptions.CustomException;
import org.fyp.emssep490be.exceptions.ErrorCode;
import org.fyp.emssep490be.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for StudentRequestServiceImpl
 * Tests all absence request operations and business logic validations
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StudentRequestService Unit Tests")
class StudentRequestServiceImplTest {

    @Mock
    private StudentRequestRepository studentRequestRepository;

    @Mock
    private StudentSessionRepository studentSessionRepository;

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private UserAccountRepository userAccountRepository;

    @InjectMocks
    private StudentRequestServiceImpl studentRequestService;

    // Test data
    private Student testStudent;
    private UserAccount testStudentAccount;
    private UserAccount testStaffAccount;
    private SessionEntity testSession;
    private ClassEntity testClass;
    private Branch testBranch;
    private CourseSession testCourseSession;
    private Enrollment testEnrollment;
    private StudentSession testStudentSession;
    private StudentRequest testAbsenceRequest;

    @BeforeEach
    void setUp() {
        // Setup test student account
        testStudentAccount = new UserAccount();
        testStudentAccount.setId(100L);
        testStudentAccount.setEmail("student@test.com");
        testStudentAccount.setFullName("Test Student");

        // Setup test student
        testStudent = new Student();
        testStudent.setId(1L);
        testStudent.setStudentCode("STU001");
        testStudent.setUserAccount(testStudentAccount);

        // Setup test staff account
        testStaffAccount = new UserAccount();
        testStaffAccount.setId(200L);
        testStaffAccount.setEmail("staff@test.com");
        testStaffAccount.setFullName("Test Staff");

        // Setup test branch
        testBranch = new Branch();
        testBranch.setId(10L);
        testBranch.setName("Test Branch");

        // Setup test class
        testClass = new ClassEntity();
        testClass.setId(50L);
        testClass.setName("English A1 Morning");
        testClass.setBranch(testBranch);

        // Setup test course session
        testCourseSession = new CourseSession();
        testCourseSession.setId(5L);
        testCourseSession.setSequenceNumber(10);
        testCourseSession.setTopic("Listening Practice");

        // Setup test session (3 days from now - meets lead time)
        testSession = new SessionEntity();
        testSession.setId(100L);
        testSession.setClazz(testClass);
        testSession.setCourseSession(testCourseSession);
        testSession.setDate(LocalDate.now().plusDays(3));
        testSession.setStartTime(LocalTime.of(9, 0));
        testSession.setEndTime(LocalTime.of(11, 30));
        testSession.setStatus(SessionStatus.PLANNED);

        // Setup test enrollment
        testEnrollment = new Enrollment();
        testEnrollment.setId(1000L);
        testEnrollment.setStudent(testStudent);
        testEnrollment.setClazz(testClass);
        testEnrollment.setStatus(EnrollmentStatus.ENROLLED);

        // Setup test student session
        testStudentSession = new StudentSession();
        StudentSessionId studentSessionId = new StudentSessionId();
        studentSessionId.setStudentId(testStudent.getId());
        studentSessionId.setSessionId(testSession.getId());
        testStudentSession.setId(studentSessionId);
        testStudentSession.setAttendanceStatus(AttendanceStatus.PLANNED);

        // Setup test absence request
        testAbsenceRequest = new StudentRequest();
        testAbsenceRequest.setId(1L);
        testAbsenceRequest.setStudent(testStudent);
        testAbsenceRequest.setTargetSession(testSession);
        testAbsenceRequest.setRequestType(StudentRequestType.ABSENCE);
        testAbsenceRequest.setStatus(RequestStatus.PENDING);
        testAbsenceRequest.setNote("Absence reason: Family emergency");
        testAbsenceRequest.setSubmittedAt(OffsetDateTime.now());
        testAbsenceRequest.setSubmittedBy(testStudentAccount);
    }

    @Nested
    @DisplayName("Create Absence Request Tests")
    class CreateAbsenceRequestTests {

        @Test
        @DisplayName("Should create absence request successfully with valid data")
        void createAbsenceRequest_Success() {
            // Given
            CreateAbsenceRequestDTO requestDTO = CreateAbsenceRequestDTO.builder()
                    .targetSessionId(testSession.getId())
                    .reason("Family emergency - need to attend urgent matter")
                    .build();

            when(studentRepository.findById(testStudent.getId())).thenReturn(Optional.of(testStudent));
            when(sessionRepository.findById(testSession.getId())).thenReturn(Optional.of(testSession));
            when(enrollmentRepository.findByStudentIdAndClazzId(testStudent.getId(), testClass.getId()))
                    .thenReturn(Optional.of(testEnrollment));
            when(studentRequestRepository.existsPendingAbsenceRequestForSession(testStudent.getId(), testSession.getId()))
                    .thenReturn(false);
            when(studentRequestRepository.countApprovedAbsenceRequestsInClass(testStudent.getId(), testClass.getId()))
                    .thenReturn(0);
            when(studentRequestRepository.save(any(StudentRequest.class))).thenReturn(testAbsenceRequest);

            // When
            StudentRequestDTO result = studentRequestService.createAbsenceRequest(testStudent.getId(), requestDTO);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(testAbsenceRequest.getId());
            assertThat(result.getRequestType()).isEqualTo(StudentRequestType.ABSENCE);
            assertThat(result.getStatus()).isEqualTo(RequestStatus.PENDING);
            assertThat(result.getStudentId()).isEqualTo(testStudent.getId());
            assertThat(result.getTargetSessionId()).isEqualTo(testSession.getId());

            // Verify save was called
            ArgumentCaptor<StudentRequest> requestCaptor = ArgumentCaptor.forClass(StudentRequest.class);
            verify(studentRequestRepository).save(requestCaptor.capture());
            StudentRequest savedRequest = requestCaptor.getValue();
            assertThat(savedRequest.getRequestType()).isEqualTo(StudentRequestType.ABSENCE);
            assertThat(savedRequest.getStatus()).isEqualTo(RequestStatus.PENDING);
        }

        @Test
        @DisplayName("Should throw exception when student not found")
        void createAbsenceRequest_StudentNotFound_ThrowsException() {
            // Given
            CreateAbsenceRequestDTO requestDTO = CreateAbsenceRequestDTO.builder()
                    .targetSessionId(testSession.getId())
                    .reason("Family emergency")
                    .build();

            when(studentRepository.findById(anyLong())).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> studentRequestService.createAbsenceRequest(999L, requestDTO))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.STUDENT_NOT_FOUND);

            verify(studentRequestRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when session not found")
        void createAbsenceRequest_SessionNotFound_ThrowsException() {
            // Given
            CreateAbsenceRequestDTO requestDTO = CreateAbsenceRequestDTO.builder()
                    .targetSessionId(999L)
                    .reason("Family emergency")
                    .build();

            when(studentRepository.findById(testStudent.getId())).thenReturn(Optional.of(testStudent));
            when(sessionRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> studentRequestService.createAbsenceRequest(testStudent.getId(), requestDTO))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SESSION_NOT_FOUND);

            verify(studentRequestRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when student not enrolled in class")
        void createAbsenceRequest_StudentNotEnrolled_ThrowsException() {
            // Given
            CreateAbsenceRequestDTO requestDTO = CreateAbsenceRequestDTO.builder()
                    .targetSessionId(testSession.getId())
                    .reason("Family emergency")
                    .build();

            when(studentRepository.findById(testStudent.getId())).thenReturn(Optional.of(testStudent));
            when(sessionRepository.findById(testSession.getId())).thenReturn(Optional.of(testSession));
            when(enrollmentRepository.findByStudentIdAndClazzId(testStudent.getId(), testClass.getId()))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> studentRequestService.createAbsenceRequest(testStudent.getId(), requestDTO))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.STUDENT_NOT_ENROLLED_IN_CLASS);

            verify(studentRequestRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when session status is not PLANNED")
        void createAbsenceRequest_SessionNotPlanned_ThrowsException() {
            // Given
            testSession.setStatus(SessionStatus.DONE);

            CreateAbsenceRequestDTO requestDTO = CreateAbsenceRequestDTO.builder()
                    .targetSessionId(testSession.getId())
                    .reason("Family emergency")
                    .build();

            when(studentRepository.findById(testStudent.getId())).thenReturn(Optional.of(testStudent));
            when(sessionRepository.findById(testSession.getId())).thenReturn(Optional.of(testSession));
            when(enrollmentRepository.findByStudentIdAndClazzId(testStudent.getId(), testClass.getId()))
                    .thenReturn(Optional.of(testEnrollment));

            // When & Then
            assertThatThrownBy(() -> studentRequestService.createAbsenceRequest(testStudent.getId(), requestDTO))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SESSION_NOT_PLANNED);

            verify(studentRequestRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when session has already occurred")
        void createAbsenceRequest_SessionAlreadyOccurred_ThrowsException() {
            // Given
            testSession.setDate(LocalDate.now().minusDays(1)); // Yesterday

            CreateAbsenceRequestDTO requestDTO = CreateAbsenceRequestDTO.builder()
                    .targetSessionId(testSession.getId())
                    .reason("Family emergency")
                    .build();

            when(studentRepository.findById(testStudent.getId())).thenReturn(Optional.of(testStudent));
            when(sessionRepository.findById(testSession.getId())).thenReturn(Optional.of(testSession));
            when(enrollmentRepository.findByStudentIdAndClazzId(testStudent.getId(), testClass.getId()))
                    .thenReturn(Optional.of(testEnrollment));

            // When & Then
            assertThatThrownBy(() -> studentRequestService.createAbsenceRequest(testStudent.getId(), requestDTO))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SESSION_ALREADY_OCCURRED);

            verify(studentRequestRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when lead time not met (session too soon)")
        void createAbsenceRequest_LeadTimeNotMet_ThrowsException() {
            // Given
            testSession.setDate(LocalDate.now().plusDays(1)); // Tomorrow (< 2 days)

            CreateAbsenceRequestDTO requestDTO = CreateAbsenceRequestDTO.builder()
                    .targetSessionId(testSession.getId())
                    .reason("Family emergency")
                    .build();

            when(studentRepository.findById(testStudent.getId())).thenReturn(Optional.of(testStudent));
            when(sessionRepository.findById(testSession.getId())).thenReturn(Optional.of(testSession));
            when(enrollmentRepository.findByStudentIdAndClazzId(testStudent.getId(), testClass.getId()))
                    .thenReturn(Optional.of(testEnrollment));

            // When & Then
            assertThatThrownBy(() -> studentRequestService.createAbsenceRequest(testStudent.getId(), requestDTO))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ABSENCE_REQUEST_LEAD_TIME_NOT_MET);

            verify(studentRequestRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when duplicate pending request exists")
        void createAbsenceRequest_DuplicatePendingRequest_ThrowsException() {
            // Given
            CreateAbsenceRequestDTO requestDTO = CreateAbsenceRequestDTO.builder()
                    .targetSessionId(testSession.getId())
                    .reason("Family emergency")
                    .build();

            when(studentRepository.findById(testStudent.getId())).thenReturn(Optional.of(testStudent));
            when(sessionRepository.findById(testSession.getId())).thenReturn(Optional.of(testSession));
            when(enrollmentRepository.findByStudentIdAndClazzId(testStudent.getId(), testClass.getId()))
                    .thenReturn(Optional.of(testEnrollment));
            when(studentRequestRepository.existsPendingAbsenceRequestForSession(testStudent.getId(), testSession.getId()))
                    .thenReturn(true); // Duplicate exists

            // When & Then
            assertThatThrownBy(() -> studentRequestService.createAbsenceRequest(testStudent.getId(), requestDTO))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_ABSENCE_REQUEST);

            verify(studentRequestRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should create request even when absence quota reached (warning only)")
        void createAbsenceRequest_QuotaReached_StillCreatesRequest() {
            // Given
            CreateAbsenceRequestDTO requestDTO = CreateAbsenceRequestDTO.builder()
                    .targetSessionId(testSession.getId())
                    .reason("Family emergency")
                    .build();

            when(studentRepository.findById(testStudent.getId())).thenReturn(Optional.of(testStudent));
            when(sessionRepository.findById(testSession.getId())).thenReturn(Optional.of(testSession));
            when(enrollmentRepository.findByStudentIdAndClazzId(testStudent.getId(), testClass.getId()))
                    .thenReturn(Optional.of(testEnrollment));
            when(studentRequestRepository.existsPendingAbsenceRequestForSession(testStudent.getId(), testSession.getId()))
                    .thenReturn(false);
            when(studentRequestRepository.countApprovedAbsenceRequestsInClass(testStudent.getId(), testClass.getId()))
                    .thenReturn(3); // Quota reached
            when(studentRequestRepository.save(any(StudentRequest.class))).thenReturn(testAbsenceRequest);

            // When
            StudentRequestDTO result = studentRequestService.createAbsenceRequest(testStudent.getId(), requestDTO);

            // Then
            assertThat(result).isNotNull();
            verify(studentRequestRepository).save(any(StudentRequest.class));
            // Note: This should log a warning, but request is still created
        }
    }

    @Nested
    @DisplayName("Approve Absence Request Tests")
    class ApproveAbsenceRequestTests {

        @Test
        @DisplayName("Should approve absence request successfully")
        void approveAbsenceRequest_Success() {
            // Given
            Long staffId = 200L;
            ApproveRequestDTO approveDTO = ApproveRequestDTO.builder()
                    .decisionNotes("Approved. Valid reason provided.")
                    .build();

            when(studentRequestRepository.findById(testAbsenceRequest.getId()))
                    .thenReturn(Optional.of(testAbsenceRequest));
            when(userAccountRepository.findById(staffId)).thenReturn(Optional.of(testStaffAccount));
            when(studentSessionRepository.findByIdStudentIdAndIdSessionId(testStudent.getId(), testSession.getId()))
                    .thenReturn(Optional.of(testStudentSession));
            when(studentSessionRepository.save(any(StudentSession.class))).thenReturn(testStudentSession);
            when(studentRequestRepository.save(any(StudentRequest.class))).thenReturn(testAbsenceRequest);

            // When
            StudentRequestDTO result = studentRequestService.approveAbsenceRequest(
                    testAbsenceRequest.getId(), staffId, approveDTO);

            // Then
            assertThat(result).isNotNull();

            // Verify student session was updated to EXCUSED
            ArgumentCaptor<StudentSession> sessionCaptor = ArgumentCaptor.forClass(StudentSession.class);
            verify(studentSessionRepository).save(sessionCaptor.capture());
            StudentSession updatedSession = sessionCaptor.getValue();
            assertThat(updatedSession.getAttendanceStatus()).isEqualTo(AttendanceStatus.EXCUSED);
            assertThat(updatedSession.getNote()).contains("Approved absence request");

            // Verify request was updated
            ArgumentCaptor<StudentRequest> requestCaptor = ArgumentCaptor.forClass(StudentRequest.class);
            verify(studentRequestRepository).save(requestCaptor.capture());
            StudentRequest updatedRequest = requestCaptor.getValue();
            assertThat(updatedRequest.getStatus()).isEqualTo(RequestStatus.APPROVED);
            assertThat(updatedRequest.getDecidedBy()).isEqualTo(testStaffAccount);
            assertThat(updatedRequest.getDecidedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should throw exception when request not found")
        void approveAbsenceRequest_RequestNotFound_ThrowsException() {
            // Given
            ApproveRequestDTO approveDTO = new ApproveRequestDTO();
            when(studentRequestRepository.findById(anyLong())).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> studentRequestService.approveAbsenceRequest(999L, 200L, approveDTO))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.STUDENT_REQUEST_NOT_FOUND);

            verify(studentSessionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when request is not PENDING")
        void approveAbsenceRequest_NotPending_ThrowsException() {
            // Given
            testAbsenceRequest.setStatus(RequestStatus.APPROVED); // Already approved

            ApproveRequestDTO approveDTO = new ApproveRequestDTO();
            when(studentRequestRepository.findById(testAbsenceRequest.getId()))
                    .thenReturn(Optional.of(testAbsenceRequest));

            // When & Then
            assertThatThrownBy(() -> studentRequestService.approveAbsenceRequest(
                    testAbsenceRequest.getId(), 200L, approveDTO))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REQUEST_NOT_PENDING);

            verify(studentSessionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when request type is not ABSENCE")
        void approveAbsenceRequest_WrongType_ThrowsException() {
            // Given
            testAbsenceRequest.setRequestType(StudentRequestType.MAKEUP); // Wrong type

            ApproveRequestDTO approveDTO = new ApproveRequestDTO();
            when(studentRequestRepository.findById(testAbsenceRequest.getId()))
                    .thenReturn(Optional.of(testAbsenceRequest));

            // When & Then
            assertThatThrownBy(() -> studentRequestService.approveAbsenceRequest(
                    testAbsenceRequest.getId(), 200L, approveDTO))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REQUEST_TYPE_MISMATCH);

            verify(studentSessionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when student session not found")
        void approveAbsenceRequest_StudentSessionNotFound_ThrowsException() {
            // Given
            ApproveRequestDTO approveDTO = new ApproveRequestDTO();
            when(studentRequestRepository.findById(testAbsenceRequest.getId()))
                    .thenReturn(Optional.of(testAbsenceRequest));
            when(studentSessionRepository.findByIdStudentIdAndIdSessionId(testStudent.getId(), testSession.getId()))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> studentRequestService.approveAbsenceRequest(
                    testAbsenceRequest.getId(), 200L, approveDTO))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.STUDENT_SESSION_NOT_FOUND);

            // Exception thrown before getUserAccount is called, so repository not saved
            verify(studentRequestRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Reject Absence Request Tests")
    class RejectAbsenceRequestTests {

        @Test
        @DisplayName("Should reject absence request successfully")
        void rejectAbsenceRequest_Success() {
            // Given
            Long staffId = 200L;
            RejectRequestDTO rejectDTO = RejectRequestDTO.builder()
                    .rejectionReason("Target class is full. Please choose another class.")
                    .build();

            when(studentRequestRepository.findById(testAbsenceRequest.getId()))
                    .thenReturn(Optional.of(testAbsenceRequest));
            when(userAccountRepository.findById(staffId)).thenReturn(Optional.of(testStaffAccount));
            when(studentRequestRepository.save(any(StudentRequest.class))).thenReturn(testAbsenceRequest);

            // When
            StudentRequestDTO result = studentRequestService.rejectAbsenceRequest(
                    testAbsenceRequest.getId(), staffId, rejectDTO);

            // Then
            assertThat(result).isNotNull();

            // Verify request was updated
            ArgumentCaptor<StudentRequest> requestCaptor = ArgumentCaptor.forClass(StudentRequest.class);
            verify(studentRequestRepository).save(requestCaptor.capture());
            StudentRequest updatedRequest = requestCaptor.getValue();
            assertThat(updatedRequest.getStatus()).isEqualTo(RequestStatus.REJECTED);
            assertThat(updatedRequest.getDecidedBy()).isEqualTo(testStaffAccount);
            assertThat(updatedRequest.getDecidedAt()).isNotNull();
            assertThat(updatedRequest.getNote()).contains("Rejection reason");
        }

        @Test
        @DisplayName("Should throw exception when request not found")
        void rejectAbsenceRequest_RequestNotFound_ThrowsException() {
            // Given
            RejectRequestDTO rejectDTO = RejectRequestDTO.builder()
                    .rejectionReason("Some reason")
                    .build();
            when(studentRequestRepository.findById(anyLong())).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> studentRequestService.rejectAbsenceRequest(999L, 200L, rejectDTO))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.STUDENT_REQUEST_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("Query Operations Tests")
    class QueryOperationsTests {

        @Test
        @DisplayName("Should get request by ID successfully")
        void getRequestById_Success() {
            // Given
            when(studentRequestRepository.findById(testAbsenceRequest.getId()))
                    .thenReturn(Optional.of(testAbsenceRequest));

            // When
            StudentRequestDTO result = studentRequestService.getRequestById(testAbsenceRequest.getId());

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(testAbsenceRequest.getId());
            verify(studentRequestRepository).findById(testAbsenceRequest.getId());
        }

        @Test
        @DisplayName("Should get student requests with filters")
        void getStudentRequests_Success() {
            // Given
            List<StudentRequest> requests = Arrays.asList(testAbsenceRequest);
            when(studentRepository.findById(testStudent.getId())).thenReturn(Optional.of(testStudent));
            when(studentRequestRepository.findByStudentIdWithFilters(
                    testStudent.getId(), StudentRequestType.ABSENCE, RequestStatus.PENDING))
                    .thenReturn(requests);

            // When
            List<StudentRequestDTO> result = studentRequestService.getStudentRequests(
                    testStudent.getId(), StudentRequestType.ABSENCE, RequestStatus.PENDING);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(testAbsenceRequest.getId());
        }

        @Test
        @DisplayName("Should get all requests with pagination")
        void getAllRequests_Success() {
            // Given
            Page<StudentRequest> page = new PageImpl<>(Arrays.asList(testAbsenceRequest));
            Pageable pageable = PageRequest.of(0, 20);

            when(studentRequestRepository.findAllWithFilters(
                    RequestStatus.PENDING, StudentRequestType.ABSENCE, null, null, pageable))
                    .thenReturn(page);

            // When
            Page<StudentRequestDTO> result = studentRequestService.getAllRequests(
                    RequestStatus.PENDING, StudentRequestType.ABSENCE, null, null, pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getId()).isEqualTo(testAbsenceRequest.getId());
        }
    }

    @Nested
    @DisplayName("Cancel Request Tests")
    class CancelRequestTests {

        @Test
        @DisplayName("Should cancel pending request successfully")
        void cancelRequest_Success() {
            // Given
            when(studentRequestRepository.findById(testAbsenceRequest.getId()))
                    .thenReturn(Optional.of(testAbsenceRequest));
            when(studentRequestRepository.save(any(StudentRequest.class))).thenReturn(testAbsenceRequest);

            // When
            StudentRequestDTO result = studentRequestService.cancelRequest(
                    testAbsenceRequest.getId(), testStudent.getId());

            // Then
            assertThat(result).isNotNull();
            ArgumentCaptor<StudentRequest> captor = ArgumentCaptor.forClass(StudentRequest.class);
            verify(studentRequestRepository).save(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(RequestStatus.CANCELLED);
        }

        @Test
        @DisplayName("Should throw exception when student is not the owner")
        void cancelRequest_NotOwner_ThrowsException() {
            // Given
            when(studentRequestRepository.findById(testAbsenceRequest.getId()))
                    .thenReturn(Optional.of(testAbsenceRequest));

            // When & Then
            assertThatThrownBy(() -> studentRequestService.cancelRequest(testAbsenceRequest.getId(), 999L))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN);

            verify(studentRequestRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when request is not pending")
        void cancelRequest_NotPending_ThrowsException() {
            // Given
            testAbsenceRequest.setStatus(RequestStatus.APPROVED);
            when(studentRequestRepository.findById(testAbsenceRequest.getId()))
                    .thenReturn(Optional.of(testAbsenceRequest));

            // When & Then
            assertThatThrownBy(() -> studentRequestService.cancelRequest(
                    testAbsenceRequest.getId(), testStudent.getId()))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REQUEST_NOT_PENDING);

            verify(studentRequestRepository, never()).save(any());
        }
    }
}
