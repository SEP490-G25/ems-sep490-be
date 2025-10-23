package org.fyp.emssep490be.services.studentrequest.impl;

import org.fyp.emssep490be.dtos.studentrequest.ApproveRequestDTO;
import org.fyp.emssep490be.dtos.studentrequest.CreateAbsenceRequestDTO;
import org.fyp.emssep490be.dtos.studentrequest.CreateMakeupRequestDTO;
import org.fyp.emssep490be.dtos.studentrequest.RejectRequestDTO;
import org.fyp.emssep490be.dtos.studentrequest.StudentRequestDTO;
import org.fyp.emssep490be.dtos.studentrequest.AvailableMakeupSessionDTO;
import org.fyp.emssep490be.dtos.studentrequest.MakeupSessionSearchResultDTO;
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
import java.util.Collections;
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
    private SessionEntity makeupSession;
    private ClassEntity testClass;
    private ClassEntity makeupClass;
    private Branch testBranch;
    private CourseSession testCourseSession;
    private Enrollment testEnrollment;
    private StudentSession testStudentSession;
    private StudentSession makeupStudentSession;
    private StudentRequest testAbsenceRequest;
    private StudentRequest testMakeupRequest;

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

            when(studentRequestRepository.findByIdWithLock(testAbsenceRequest.getId()))
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
            when(studentRequestRepository.findByIdWithLock(anyLong())).thenReturn(Optional.empty());

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
            when(studentRequestRepository.findByIdWithLock(testAbsenceRequest.getId()))
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
            when(studentRequestRepository.findByIdWithLock(testAbsenceRequest.getId()))
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
            when(studentRequestRepository.findByIdWithLock(testAbsenceRequest.getId()))
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

            when(studentRequestRepository.findByIdWithLock(testAbsenceRequest.getId()))
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
            when(studentRequestRepository.findByIdWithLock(anyLong())).thenReturn(Optional.empty());

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
            when(studentRequestRepository.findByIdWithLock(testAbsenceRequest.getId()))
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
            when(studentRequestRepository.findByIdWithLock(testAbsenceRequest.getId()))
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
            when(studentRequestRepository.findByIdWithLock(testAbsenceRequest.getId()))
                    .thenReturn(Optional.of(testAbsenceRequest));

            // When & Then
            assertThatThrownBy(() -> studentRequestService.cancelRequest(
                    testAbsenceRequest.getId(), testStudent.getId()))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REQUEST_NOT_PENDING);

            verify(studentRequestRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Find Available Makeup Sessions Tests")
    class FindAvailableMakeupSessionsTests {

        private SessionEntity missedSession;

        @BeforeEach
        void setupMakeupData() {
            // Setup missed session
            missedSession = new SessionEntity();
            missedSession.setId(10L);
            missedSession.setDate(LocalDate.now().minusDays(3));
            missedSession.setStartTime(LocalTime.of(9, 0));
            missedSession.setEndTime(LocalTime.of(11, 0));
            missedSession.setStatus(SessionStatus.DONE);
            missedSession.setClazz(testClass);
            missedSession.setCourseSession(testCourseSession);

            // Setup makeup session
            makeupSession = new SessionEntity();
            makeupSession.setId(20L);
            makeupSession.setDate(LocalDate.now().plusDays(7));
            makeupSession.setStartTime(LocalTime.of(14, 0));
            makeupSession.setEndTime(LocalTime.of(16, 0));
            makeupSession.setStatus(SessionStatus.PLANNED);
            
            makeupClass = new ClassEntity();
            makeupClass.setId(2L);
            makeupClass.setName("Test Class B");
            makeupClass.setMaxCapacity(30);
            makeupClass.setBranch(testBranch);
            
            makeupSession.setClazz(makeupClass);
            makeupSession.setCourseSession(testCourseSession);

            // Setup student session for missed session
            testStudentSession = new StudentSession();
            testStudentSession.setId(new StudentSessionId(testStudent.getId(), missedSession.getId()));
            testStudentSession.setStudent(testStudent);
            testStudentSession.setSession(missedSession);
            testStudentSession.setAttendanceStatus(AttendanceStatus.ABSENT);
        }

        @Test
        @DisplayName("Should find available makeup sessions successfully")
        void findAvailableMakeupSessions_Success() {
            // Given
            when(studentRepository.findById(testStudent.getId())).thenReturn(Optional.of(testStudent));
            when(sessionRepository.findById(missedSession.getId())).thenReturn(Optional.of(missedSession));
            when(studentSessionRepository.findByIdStudentIdAndIdSessionId(testStudent.getId(), missedSession.getId()))
                    .thenReturn(Optional.of(testStudentSession));

            Object[] mockResult = {makeupSession, makeupClass, testBranch, testCourseSession, 5L, 25L};
            when(sessionRepository.findAvailableMakeupSessions(
                    eq(testCourseSession.getId()),
                    eq(testStudent.getId()),
                    any(),  // dateFrom can be null or LocalDate
                    any(),  // dateTo can be null or LocalDate
                    any(),  // branchId can be null or Long
                    any()   // modality can be null or Modality
            )).thenReturn(Collections.singletonList(mockResult));

            // When
            MakeupSessionSearchResultDTO result = studentRequestService.findAvailableMakeupSessions(
                    testStudent.getId(), missedSession.getId(), null, null, null, null);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTotal()).isEqualTo(1);
            assertThat(result.getMakeupSessions()).hasSize(1);
            
            AvailableMakeupSessionDTO session = result.getMakeupSessions().get(0);
            assertThat(session.getSessionId()).isEqualTo(makeupSession.getId());
            assertThat(session.getAvailableSlots()).isEqualTo(5);
            assertThat(session.getEnrolledCount()).isEqualTo(25);
        }

        @Test
        @DisplayName("Should return empty list when no sessions available")
        void findAvailableMakeupSessions_NoResults() {
            // Given
            when(studentRepository.findById(testStudent.getId())).thenReturn(Optional.of(testStudent));
            when(sessionRepository.findById(missedSession.getId())).thenReturn(Optional.of(missedSession));
            when(studentSessionRepository.findByIdStudentIdAndIdSessionId(testStudent.getId(), missedSession.getId()))
                    .thenReturn(Optional.of(testStudentSession));
            when(sessionRepository.findAvailableMakeupSessions(anyLong(), anyLong(), any(), any(), any(), any()))
                    .thenReturn(Collections.emptyList());

            // When
            MakeupSessionSearchResultDTO result = studentRequestService.findAvailableMakeupSessions(
                    testStudent.getId(), missedSession.getId(), null, null, null, null);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTotal()).isZero();
            assertThat(result.getMakeupSessions()).isEmpty();
        }

        @Test
        @DisplayName("Should throw exception when student not found")
        void findAvailableMakeupSessions_StudentNotFound() {
            // Given
            when(studentRepository.findById(testStudent.getId())).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> studentRequestService.findAvailableMakeupSessions(
                    testStudent.getId(), missedSession.getId(), null, null, null, null))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.STUDENT_NOT_FOUND);
        }

        @Test
        @DisplayName("Should throw exception when session not found")
        void findAvailableMakeupSessions_SessionNotFound() {
            // Given
            when(studentRepository.findById(testStudent.getId())).thenReturn(Optional.of(testStudent));
            when(sessionRepository.findById(missedSession.getId())).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> studentRequestService.findAvailableMakeupSessions(
                    testStudent.getId(), missedSession.getId(), null, null, null, null))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SESSION_NOT_FOUND);
        }

        @Test
        @DisplayName("Should filter by date range")
        void findAvailableMakeupSessions_FilterByDateRange() {
            // Given
            LocalDate dateFrom = LocalDate.now().plusDays(1);
            LocalDate dateTo = LocalDate.now().plusDays(14);
            
            when(studentRepository.findById(testStudent.getId())).thenReturn(Optional.of(testStudent));
            when(sessionRepository.findById(missedSession.getId())).thenReturn(Optional.of(missedSession));
            when(studentSessionRepository.findByIdStudentIdAndIdSessionId(testStudent.getId(), missedSession.getId()))
                    .thenReturn(Optional.of(testStudentSession));
            when(sessionRepository.findAvailableMakeupSessions(
                    eq(testCourseSession.getId()),
                    eq(testStudent.getId()),
                    eq(dateFrom),
                    eq(dateTo),
                    isNull(),
                    isNull()
            )).thenReturn(Collections.emptyList());

            // When
            studentRequestService.findAvailableMakeupSessions(
                    testStudent.getId(), missedSession.getId(), dateFrom, dateTo, null, null);

            // Then
            verify(sessionRepository).findAvailableMakeupSessions(
                    eq(testCourseSession.getId()),
                    eq(testStudent.getId()),
                    eq(dateFrom),
                    eq(dateTo),
                    any(),
                    any()
            );
        }
    }

    @Nested
    @DisplayName("Create Makeup Request Tests")
    class CreateMakeupRequestTests {

        private CreateMakeupRequestDTO makeupRequestDTO;

        @BeforeEach
        void setupMakeupRequest() {
            // Setup target (missed) session
            testSession.setDate(LocalDate.now().minusDays(3));
            testSession.setStatus(SessionStatus.DONE);
            testSession.setCourseSession(testCourseSession);

            // Setup makeup session
            makeupSession = new SessionEntity();
            makeupSession.setId(20L);
            makeupSession.setDate(LocalDate.now().plusDays(7));
            makeupSession.setStartTime(LocalTime.of(14, 0));
            makeupSession.setEndTime(LocalTime.of(16, 0));
            makeupSession.setStatus(SessionStatus.PLANNED);
            makeupSession.setCourseSession(testCourseSession); // SAME course session
            
            makeupClass = new ClassEntity();
            makeupClass.setId(2L);
            makeupClass.setMaxCapacity(30);
            makeupSession.setClazz(makeupClass);

            // Setup student session
            testStudentSession = new StudentSession();
            testStudentSession.setId(new StudentSessionId(testStudent.getId(), testSession.getId()));
            testStudentSession.setStudent(testStudent);
            testStudentSession.setSession(testSession);
            testStudentSession.setAttendanceStatus(AttendanceStatus.ABSENT);

            // Setup DTO
            makeupRequestDTO = new CreateMakeupRequestDTO();
            makeupRequestDTO.setTargetSessionId(testSession.getId());
            makeupRequestDTO.setMakeupSessionId(makeupSession.getId());
            makeupRequestDTO.setReason("I was sick and need to make up the lesson");
        }

        @Test
        @DisplayName("Should create makeup request successfully")
        void createMakeupRequest_Success() {
            // Given
            when(studentRepository.findById(testStudent.getId())).thenReturn(Optional.of(testStudent));
            when(sessionRepository.findById(testSession.getId())).thenReturn(Optional.of(testSession));
            when(sessionRepository.findById(makeupSession.getId())).thenReturn(Optional.of(makeupSession));
            when(studentSessionRepository.findByIdStudentIdAndIdSessionId(testStudent.getId(), testSession.getId()))
                    .thenReturn(Optional.of(testStudentSession));
            when(studentSessionRepository.findByIdStudentIdAndIdSessionId(testStudent.getId(), makeupSession.getId()))
                    .thenReturn(Optional.empty());
            when(sessionRepository.countEnrolledStudents(makeupSession.getId())).thenReturn(25L);
            when(studentSessionRepository.countScheduleConflicts(
                    eq(testStudent.getId()), 
                    eq(makeupSession.getDate()), 
                    eq(makeupSession.getStartTime()), 
                    eq(makeupSession.getEndTime())
            )).thenReturn(0L);
            when(studentSessionRepository.countMakeupSessions(testStudent.getId(), testClass.getId())).thenReturn(3L);
            when(studentRequestRepository.existsPendingMakeupRequest(
                    testStudent.getId(), testSession.getId(), makeupSession.getId()
            )).thenReturn(false);
            
            StudentRequest savedRequest = new StudentRequest();
            savedRequest.setId(100L);
            savedRequest.setStudent(testStudent);
            savedRequest.setRequestType(StudentRequestType.MAKEUP);
            savedRequest.setStatus(RequestStatus.PENDING);
            when(studentRequestRepository.save(any(StudentRequest.class))).thenReturn(savedRequest);

            // When
            StudentRequestDTO result = studentRequestService.createMakeupRequest(testStudent.getId(), makeupRequestDTO);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getRequestType()).isEqualTo(StudentRequestType.MAKEUP);
            
            ArgumentCaptor<StudentRequest> captor = ArgumentCaptor.forClass(StudentRequest.class);
            verify(studentRequestRepository).save(captor.capture());
            StudentRequest captured = captor.getValue();
            assertThat(captured.getTargetSession().getId()).isEqualTo(testSession.getId());
            assertThat(captured.getMakeupSession().getId()).isEqualTo(makeupSession.getId());
            assertThat(captured.getStatus()).isEqualTo(RequestStatus.PENDING);
        }

        @Test
        @DisplayName("Should throw exception when student not found")
        void createMakeupRequest_StudentNotFound() {
            // Given
            when(studentRepository.findById(testStudent.getId())).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> studentRequestService.createMakeupRequest(testStudent.getId(), makeupRequestDTO))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.STUDENT_NOT_FOUND);
        }

        @Test
        @DisplayName("Should throw exception when target session not found")
        void createMakeupRequest_TargetSessionNotFound() {
            // Given
            when(studentRepository.findById(testStudent.getId())).thenReturn(Optional.of(testStudent));
            when(sessionRepository.findById(testSession.getId())).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> studentRequestService.createMakeupRequest(testStudent.getId(), makeupRequestDTO))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SESSION_NOT_FOUND);
        }

        @Test
        @DisplayName("Should throw exception when makeup session not found")
        void createMakeupRequest_MakeupSessionNotFound() {
            // Given
            when(studentRepository.findById(testStudent.getId())).thenReturn(Optional.of(testStudent));
            when(sessionRepository.findById(testSession.getId())).thenReturn(Optional.of(testSession));
            when(sessionRepository.findById(makeupSession.getId())).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> studentRequestService.createMakeupRequest(testStudent.getId(), makeupRequestDTO))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SESSION_NOT_FOUND);
        }

        @Test
        @DisplayName("Should throw exception when student session not found")
        void createMakeupRequest_StudentSessionNotFound() {
            // Given
            when(studentRepository.findById(testStudent.getId())).thenReturn(Optional.of(testStudent));
            when(sessionRepository.findById(testSession.getId())).thenReturn(Optional.of(testSession));
            when(sessionRepository.findById(makeupSession.getId())).thenReturn(Optional.of(makeupSession));
            // StudentSession not found - will throw exception before checking makeup session
            when(studentSessionRepository.findByIdStudentIdAndIdSessionId(testStudent.getId(), testSession.getId()))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> studentRequestService.createMakeupRequest(testStudent.getId(), makeupRequestDTO))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.STUDENT_SESSION_NOT_FOUND);
        }

        @Test
        @DisplayName("Should throw exception when attendance status is invalid")
        void createMakeupRequest_InvalidAttendanceStatus() {
            // Given
            testStudentSession.setAttendanceStatus(AttendanceStatus.PRESENT);
            
            when(studentRepository.findById(testStudent.getId())).thenReturn(Optional.of(testStudent));
            when(sessionRepository.findById(testSession.getId())).thenReturn(Optional.of(testSession));
            when(sessionRepository.findById(makeupSession.getId())).thenReturn(Optional.of(makeupSession));
            when(studentSessionRepository.findByIdStudentIdAndIdSessionId(testStudent.getId(), testSession.getId()))
                    .thenReturn(Optional.of(testStudentSession));

            // When & Then
            assertThatThrownBy(() -> studentRequestService.createMakeupRequest(testStudent.getId(), makeupRequestDTO))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_ATTENDANCE_STATUS_FOR_MAKEUP);
        }

        @Test
        @DisplayName("Should throw exception when course session mismatch - CRITICAL TEST")
        void createMakeupRequest_CourseSessionMismatch() {
            // Given
            CourseSession differentCourseSession = new CourseSession();
            differentCourseSession.setId(999L); // Different course session!
            makeupSession.setCourseSession(differentCourseSession);
            
            when(studentRepository.findById(testStudent.getId())).thenReturn(Optional.of(testStudent));
            when(sessionRepository.findById(testSession.getId())).thenReturn(Optional.of(testSession));
            when(sessionRepository.findById(makeupSession.getId())).thenReturn(Optional.of(makeupSession));
            when(studentSessionRepository.findByIdStudentIdAndIdSessionId(testStudent.getId(), testSession.getId()))
                    .thenReturn(Optional.of(testStudentSession));

            // When & Then
            assertThatThrownBy(() -> studentRequestService.createMakeupRequest(testStudent.getId(), makeupRequestDTO))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MAKEUP_COURSE_SESSION_MISMATCH);
        }

        @Test
        @DisplayName("Should throw exception when makeup session not planned")
        void createMakeupRequest_MakeupSessionNotPlanned() {
            // Given
            makeupSession.setStatus(SessionStatus.DONE);
            
            when(studentRepository.findById(testStudent.getId())).thenReturn(Optional.of(testStudent));
            when(sessionRepository.findById(testSession.getId())).thenReturn(Optional.of(testSession));
            when(sessionRepository.findById(makeupSession.getId())).thenReturn(Optional.of(makeupSession));
            when(studentSessionRepository.findByIdStudentIdAndIdSessionId(testStudent.getId(), testSession.getId()))
                    .thenReturn(Optional.of(testStudentSession));

            // When & Then
            assertThatThrownBy(() -> studentRequestService.createMakeupRequest(testStudent.getId(), makeupRequestDTO))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SESSION_NOT_PLANNED);
        }

        @Test
        @DisplayName("Should throw exception when makeup session in past")
        void createMakeupRequest_MakeupSessionInPast() {
            // Given
            makeupSession.setDate(LocalDate.now().minusDays(1));
            
            when(studentRepository.findById(testStudent.getId())).thenReturn(Optional.of(testStudent));
            when(sessionRepository.findById(testSession.getId())).thenReturn(Optional.of(testSession));
            when(sessionRepository.findById(makeupSession.getId())).thenReturn(Optional.of(makeupSession));
            when(studentSessionRepository.findByIdStudentIdAndIdSessionId(testStudent.getId(), testSession.getId()))
                    .thenReturn(Optional.of(testStudentSession));

            // When & Then
            assertThatThrownBy(() -> studentRequestService.createMakeupRequest(testStudent.getId(), makeupRequestDTO))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SESSION_ALREADY_OCCURRED);
        }

        @Test
        @DisplayName("Should throw exception when already enrolled in makeup session")
        void createMakeupRequest_AlreadyEnrolled() {
            // Given
            when(studentRepository.findById(testStudent.getId())).thenReturn(Optional.of(testStudent));
            when(sessionRepository.findById(testSession.getId())).thenReturn(Optional.of(testSession));
            when(sessionRepository.findById(makeupSession.getId())).thenReturn(Optional.of(makeupSession));
            when(studentSessionRepository.findByIdStudentIdAndIdSessionId(testStudent.getId(), testSession.getId()))
                    .thenReturn(Optional.of(testStudentSession));
            when(studentSessionRepository.findByIdStudentIdAndIdSessionId(testStudent.getId(), makeupSession.getId()))
                    .thenReturn(Optional.of(new StudentSession())); // Already enrolled!

            // When & Then
            assertThatThrownBy(() -> studentRequestService.createMakeupRequest(testStudent.getId(), makeupRequestDTO))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.STUDENT_ALREADY_ENROLLED_IN_MAKEUP);
        }

        @Test
        @DisplayName("Should throw exception when capacity full - CRITICAL TEST")
        void createMakeupRequest_CapacityFull() {
            // Given
            when(studentRepository.findById(testStudent.getId())).thenReturn(Optional.of(testStudent));
            when(sessionRepository.findById(testSession.getId())).thenReturn(Optional.of(testSession));
            when(sessionRepository.findById(makeupSession.getId())).thenReturn(Optional.of(makeupSession));
            when(studentSessionRepository.findByIdStudentIdAndIdSessionId(testStudent.getId(), testSession.getId()))
                    .thenReturn(Optional.of(testStudentSession));
            when(studentSessionRepository.findByIdStudentIdAndIdSessionId(testStudent.getId(), makeupSession.getId()))
                    .thenReturn(Optional.empty());
            when(sessionRepository.countEnrolledStudents(makeupSession.getId())).thenReturn(30L); // At capacity!

            // When & Then
            assertThatThrownBy(() -> studentRequestService.createMakeupRequest(testStudent.getId(), makeupRequestDTO))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MAKEUP_SESSION_CAPACITY_FULL);
        }

        @Test
        @DisplayName("Should throw exception when schedule conflict - CRITICAL TEST")
        void createMakeupRequest_ScheduleConflict() {
            // Given
            when(studentRepository.findById(testStudent.getId())).thenReturn(Optional.of(testStudent));
            when(sessionRepository.findById(testSession.getId())).thenReturn(Optional.of(testSession));
            when(sessionRepository.findById(makeupSession.getId())).thenReturn(Optional.of(makeupSession));
            when(studentSessionRepository.findByIdStudentIdAndIdSessionId(testStudent.getId(), testSession.getId()))
                    .thenReturn(Optional.of(testStudentSession));
            when(studentSessionRepository.findByIdStudentIdAndIdSessionId(testStudent.getId(), makeupSession.getId()))
                    .thenReturn(Optional.empty());
            when(sessionRepository.countEnrolledStudents(makeupSession.getId())).thenReturn(25L);
            when(studentSessionRepository.countScheduleConflicts(
                    eq(testStudent.getId()), 
                    eq(makeupSession.getDate()), 
                    eq(makeupSession.getStartTime()), 
                    eq(makeupSession.getEndTime())
            )).thenReturn(1L); // Conflict detected!

            // When & Then
            assertThatThrownBy(() -> studentRequestService.createMakeupRequest(testStudent.getId(), makeupRequestDTO))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SCHEDULE_CONFLICT);
        }

        @Test
        @DisplayName("Should throw exception when makeup quota exceeded - CRITICAL TEST")
        void createMakeupRequest_QuotaExceeded() {
            // Given
            when(studentRepository.findById(testStudent.getId())).thenReturn(Optional.of(testStudent));
            when(sessionRepository.findById(testSession.getId())).thenReturn(Optional.of(testSession));
            when(sessionRepository.findById(makeupSession.getId())).thenReturn(Optional.of(makeupSession));
            when(studentSessionRepository.findByIdStudentIdAndIdSessionId(testStudent.getId(), testSession.getId()))
                    .thenReturn(Optional.of(testStudentSession));
            when(studentSessionRepository.findByIdStudentIdAndIdSessionId(testStudent.getId(), makeupSession.getId()))
                    .thenReturn(Optional.empty());
            when(sessionRepository.countEnrolledStudents(makeupSession.getId())).thenReturn(25L);
            when(studentSessionRepository.countScheduleConflicts(anyLong(), any(), any(), any())).thenReturn(0L);
            when(studentSessionRepository.countMakeupSessions(testStudent.getId(), testClass.getId()))
                    .thenReturn(5L); // Quota exceeded!

            // When & Then
            assertThatThrownBy(() -> studentRequestService.createMakeupRequest(testStudent.getId(), makeupRequestDTO))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MAKEUP_QUOTA_EXCEEDED);
        }

        @Test
        @DisplayName("Should throw exception when duplicate pending request exists")
        void createMakeupRequest_DuplicateRequest() {
            // Given
            when(studentRepository.findById(testStudent.getId())).thenReturn(Optional.of(testStudent));
            when(sessionRepository.findById(testSession.getId())).thenReturn(Optional.of(testSession));
            when(sessionRepository.findById(makeupSession.getId())).thenReturn(Optional.of(makeupSession));
            when(studentSessionRepository.findByIdStudentIdAndIdSessionId(testStudent.getId(), testSession.getId()))
                    .thenReturn(Optional.of(testStudentSession));
            when(studentSessionRepository.findByIdStudentIdAndIdSessionId(testStudent.getId(), makeupSession.getId()))
                    .thenReturn(Optional.empty());
            when(sessionRepository.countEnrolledStudents(makeupSession.getId())).thenReturn(25L);
            when(studentSessionRepository.countScheduleConflicts(anyLong(), any(), any(), any())).thenReturn(0L);
            when(studentSessionRepository.countMakeupSessions(testStudent.getId(), testClass.getId())).thenReturn(3L);
            when(studentRequestRepository.existsPendingMakeupRequest(
                    testStudent.getId(), testSession.getId(), makeupSession.getId()
            )).thenReturn(true); // Duplicate!

            // When & Then
            assertThatThrownBy(() -> studentRequestService.createMakeupRequest(testStudent.getId(), makeupRequestDTO))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_ABSENCE_REQUEST);
        }
    }

    @Nested
    @DisplayName("Approve Makeup Request Tests")
    class ApproveMakeupRequestTests {

        @BeforeEach
        void setupMakeupApproval() {
            // Setup target session
            testSession.setDate(LocalDate.now().minusDays(3));
            testSession.setStatus(SessionStatus.DONE);
            testSession.setCourseSession(testCourseSession);
            testSession.setClazz(testClass);

            // Setup makeup session
            makeupSession = new SessionEntity();
            makeupSession.setId(20L);
            makeupSession.setDate(LocalDate.now().plusDays(7));
            makeupSession.setStartTime(LocalTime.of(14, 0));
            makeupSession.setEndTime(LocalTime.of(16, 0));
            makeupSession.setStatus(SessionStatus.PLANNED);
            makeupSession.setCourseSession(testCourseSession);
            
            makeupClass = new ClassEntity();
            makeupClass.setId(2L);
            makeupClass.setName("Makeup Class");
            makeupClass.setMaxCapacity(30);
            makeupSession.setClazz(makeupClass);

            // Setup student session
            testStudentSession = new StudentSession();
            testStudentSession.setId(new StudentSessionId(testStudent.getId(), testSession.getId()));
            testStudentSession.setStudent(testStudent);
            testStudentSession.setSession(testSession);
            testStudentSession.setAttendanceStatus(AttendanceStatus.ABSENT);

            // Setup makeup request
            testMakeupRequest = new StudentRequest();
            testMakeupRequest.setId(200L);
            testMakeupRequest.setStudent(testStudent);
            testMakeupRequest.setRequestType(StudentRequestType.MAKEUP);
            testMakeupRequest.setStatus(RequestStatus.PENDING);
            testMakeupRequest.setTargetSession(testSession);
            testMakeupRequest.setMakeupSession(makeupSession);
            testMakeupRequest.setSubmittedBy(testStudentAccount);
            testMakeupRequest.setSubmittedAt(OffsetDateTime.now());
        }

        @Test
        @DisplayName("Should approve makeup request successfully")
        void approveMakeupRequest_Success() {
            // Given
            ApproveRequestDTO approveDTO = new ApproveRequestDTO();
            approveDTO.setDecisionNotes("Approved");
            
            when(studentRequestRepository.findByIdWithLock(testMakeupRequest.getId()))
                    .thenReturn(Optional.of(testMakeupRequest));
            when(userAccountRepository.findById(anyLong())).thenReturn(Optional.of(testStaffAccount));
            when(sessionRepository.findByIdWithLock(makeupSession.getId())).thenReturn(Optional.of(makeupSession));
            when(sessionRepository.countEnrolledStudents(makeupSession.getId())).thenReturn(25L);
            when(studentSessionRepository.countScheduleConflicts(
                    eq(testStudent.getId()), 
                    eq(makeupSession.getDate()), 
                    eq(makeupSession.getStartTime()), 
                    eq(makeupSession.getEndTime())
            )).thenReturn(0L);
            when(studentSessionRepository.findByIdStudentIdAndIdSessionId(testStudent.getId(), testSession.getId()))
                    .thenReturn(Optional.of(testStudentSession));
            when(studentSessionRepository.save(any(StudentSession.class))).thenAnswer(i -> i.getArgument(0));
            when(studentRequestRepository.save(any(StudentRequest.class))).thenReturn(testMakeupRequest);

            // When
            StudentRequestDTO result = studentRequestService.approveMakeupRequest(
                    testMakeupRequest.getId(), 1L, approveDTO);

            // Then
            assertThat(result).isNotNull();
            
            // Verify original session marked as excused
            ArgumentCaptor<StudentSession> sessionCaptor = ArgumentCaptor.forClass(StudentSession.class);
            verify(studentSessionRepository, times(2)).save(sessionCaptor.capture());
            List<StudentSession> savedSessions = sessionCaptor.getAllValues();
            
            // First save should be original session marked EXCUSED
            StudentSession originalSession = savedSessions.get(0);
            assertThat(originalSession.getAttendanceStatus()).isEqualTo(AttendanceStatus.EXCUSED);
            assertThat(originalSession.getNote()).contains("Approved makeup");
            
            // Second save should be new makeup session
            StudentSession newMakeupSession = savedSessions.get(1);
            assertThat(newMakeupSession.getIsMakeup()).isTrue();
            assertThat(newMakeupSession.getAttendanceStatus()).isEqualTo(AttendanceStatus.PLANNED);
            assertThat(newMakeupSession.getSession().getId()).isEqualTo(makeupSession.getId());
            
            // Verify request updated
            ArgumentCaptor<StudentRequest> requestCaptor = ArgumentCaptor.forClass(StudentRequest.class);
            verify(studentRequestRepository).save(requestCaptor.capture());
            assertThat(requestCaptor.getValue().getStatus()).isEqualTo(RequestStatus.APPROVED);
        }

        @Test
        @DisplayName("Should throw exception when request not found")
        void approveMakeupRequest_NotFound() {
            // Given
            when(studentRequestRepository.findByIdWithLock(anyLong())).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> studentRequestService.approveMakeupRequest(999L, 1L, new ApproveRequestDTO()))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.STUDENT_REQUEST_NOT_FOUND);
        }

        @Test
        @DisplayName("Should throw exception when request not pending")
        void approveMakeupRequest_NotPending() {
            // Given
            testMakeupRequest.setStatus(RequestStatus.APPROVED);
            when(studentRequestRepository.findByIdWithLock(testMakeupRequest.getId()))
                    .thenReturn(Optional.of(testMakeupRequest));

            // When & Then
            assertThatThrownBy(() -> studentRequestService.approveMakeupRequest(
                    testMakeupRequest.getId(), 1L, new ApproveRequestDTO()))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REQUEST_NOT_PENDING);
        }

        @Test
        @DisplayName("Should throw exception when request type wrong")
        void approveMakeupRequest_WrongType() {
            // Given
            testMakeupRequest.setRequestType(StudentRequestType.ABSENCE);
            when(studentRequestRepository.findByIdWithLock(testMakeupRequest.getId()))
                    .thenReturn(Optional.of(testMakeupRequest));

            // When & Then
            assertThatThrownBy(() -> studentRequestService.approveMakeupRequest(
                    testMakeupRequest.getId(), 1L, new ApproveRequestDTO()))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REQUEST_TYPE_MISMATCH);
        }

        @Test
        @DisplayName("Should throw exception when capacity changed (race condition) - CRITICAL TEST")
        void approveMakeupRequest_CapacityChangedDuringApproval() {
            // Given
            when(studentRequestRepository.findByIdWithLock(testMakeupRequest.getId()))
                    .thenReturn(Optional.of(testMakeupRequest));
            when(userAccountRepository.findById(anyLong())).thenReturn(Optional.of(testStaffAccount));
            when(sessionRepository.findByIdWithLock(makeupSession.getId())).thenReturn(Optional.of(makeupSession));
            when(sessionRepository.countEnrolledStudents(makeupSession.getId()))
                    .thenReturn(30L); // Now full!

            // When & Then
            assertThatThrownBy(() -> studentRequestService.approveMakeupRequest(
                    testMakeupRequest.getId(), 1L, new ApproveRequestDTO()))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MAKEUP_SESSION_NOW_FULL);
        }

        @Test
        @DisplayName("Should throw exception when conflict appeared (race condition) - CRITICAL TEST")
        void approveMakeupRequest_ConflictAppearedDuringApproval() {
            // Given
            when(studentRequestRepository.findByIdWithLock(testMakeupRequest.getId()))
                    .thenReturn(Optional.of(testMakeupRequest));
            when(userAccountRepository.findById(anyLong())).thenReturn(Optional.of(testStaffAccount));
            when(sessionRepository.findByIdWithLock(makeupSession.getId())).thenReturn(Optional.of(makeupSession));
            when(sessionRepository.countEnrolledStudents(makeupSession.getId())).thenReturn(25L);
            when(studentSessionRepository.countScheduleConflicts(
                    eq(testStudent.getId()), 
                    eq(makeupSession.getDate()), 
                    eq(makeupSession.getStartTime()), 
                    eq(makeupSession.getEndTime())
            )).thenReturn(1L); // Conflict appeared!

            // When & Then
            assertThatThrownBy(() -> studentRequestService.approveMakeupRequest(
                    testMakeupRequest.getId(), 1L, new ApproveRequestDTO()))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SCHEDULE_CONFLICT);
        }
    }
}
