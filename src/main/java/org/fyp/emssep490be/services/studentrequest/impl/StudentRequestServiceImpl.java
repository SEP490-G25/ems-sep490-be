package org.fyp.emssep490be.services.studentrequest.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fyp.emssep490be.dtos.studentrequest.ApproveRequestDTO;
import org.fyp.emssep490be.dtos.studentrequest.CreateAbsenceRequestDTO;
import org.fyp.emssep490be.dtos.studentrequest.RejectRequestDTO;
import org.fyp.emssep490be.dtos.studentrequest.SessionBasicDTO;
import org.fyp.emssep490be.dtos.studentrequest.StudentRequestDTO;
import org.fyp.emssep490be.entities.*;
import org.fyp.emssep490be.entities.enums.*;
import org.fyp.emssep490be.exceptions.CustomException;
import org.fyp.emssep490be.exceptions.ErrorCode;
import org.fyp.emssep490be.repositories.*;
import org.fyp.emssep490be.services.studentrequest.StudentRequestService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of StudentRequestService
 * Handles business logic for student requests (absence, makeup, transfer)
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StudentRequestServiceImpl implements StudentRequestService {

    private final StudentRequestRepository studentRequestRepository;
    private final StudentSessionRepository studentSessionRepository;
    private final SessionRepository sessionRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final UserAccountRepository userAccountRepository;

    // TODO: These should be configurable from system settings
    private static final int REQUEST_LEAD_TIME_DAYS = 2;
    private static final int MAX_ABSENCE_QUOTA_PER_CLASS = 3;

    // ==================== ABSENCE REQUEST OPERATIONS ====================

    @Override
    public StudentRequestDTO createAbsenceRequest(Long studentId, CreateAbsenceRequestDTO request) {
        log.info("Creating absence request for student {} for session {}", studentId, request.getTargetSessionId());

        // Step 1: Validate student exists
        Student student = validateStudentExists(studentId);

        // Step 2: Validate session exists and get details
        SessionEntity session = validateSessionExists(request.getTargetSessionId());

        // Step 3: Validate business rules
        validateAbsenceRequestRules(student, session);

        // Step 4: Create and save request
        StudentRequest absenceRequest = buildAbsenceRequest(student, session, request);
        StudentRequest saved = studentRequestRepository.save(absenceRequest);

        log.info("Absence request created with ID: {}", saved.getId());
        return mapToDTO(saved);
    }

    @Override
    public StudentRequestDTO approveAbsenceRequest(Long requestId, Long staffId, ApproveRequestDTO dto) {
        log.info("Approving absence request {} by staff {}", requestId, staffId);

        // Step 1: Get request and validate
        StudentRequest request = validateRequestExists(requestId);
        validateRequestIsPending(request);
        validateRequestIsAbsence(request);

        // Step 2: Update student_session to 'excused'
        updateStudentSessionToExcused(request);

        // Step 3: Update request status
        request.setStatus(RequestStatus.APPROVED);
        request.setDecidedBy(getUserAccount(staffId));
        request.setDecidedAt(OffsetDateTime.now());
        if (dto.getDecisionNotes() != null && !dto.getDecisionNotes().isBlank()) {
            request.setNote(request.getNote() + " | Staff notes: " + dto.getDecisionNotes());
        }

        StudentRequest updated = studentRequestRepository.save(request);
        log.info("Absence request {} approved successfully", requestId);

        return mapToDTO(updated);
    }

    @Override
    public StudentRequestDTO rejectAbsenceRequest(Long requestId, Long staffId, RejectRequestDTO dto) {
        log.info("Rejecting absence request {} by staff {}", requestId, staffId);

        // Step 1: Get request and validate
        StudentRequest request = validateRequestExists(requestId);
        validateRequestIsPending(request);
        validateRequestIsAbsence(request);

        // Step 2: Update request status
        request.setStatus(RequestStatus.REJECTED);
        request.setDecidedBy(getUserAccount(staffId));
        request.setDecidedAt(OffsetDateTime.now());
        request.setNote(request.getNote() + " | Rejection reason: " + dto.getRejectionReason());

        StudentRequest updated = studentRequestRepository.save(request);
        log.info("Absence request {} rejected", requestId);

        return mapToDTO(updated);
    }

    // ==================== QUERY OPERATIONS ====================

    @Override
    @Transactional(readOnly = true)
    public StudentRequestDTO getRequestById(Long requestId) {
        log.info("Getting request by ID: {}", requestId);
        StudentRequest request = validateRequestExists(requestId);
        return mapToDTO(request);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentRequestDTO> getStudentRequests(Long studentId, StudentRequestType type, RequestStatus status) {
        log.info("Getting requests for student {} (type={}, status={})", studentId, type, status);

        // Validate student exists
        validateStudentExists(studentId);

        List<StudentRequest> requests = studentRequestRepository.findByStudentIdWithFilters(studentId, type, status);
        return requests.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StudentRequestDTO> getAllRequests(
            RequestStatus status,
            StudentRequestType type,
            Long branchId,
            Long studentId,
            Pageable pageable) {

        log.info("Getting all requests with filters (status={}, type={}, branchId={}, studentId={})",
                status, type, branchId, studentId);

        Page<StudentRequest> requests = studentRequestRepository.findAllWithFilters(status, type, branchId, studentId, pageable);
        return requests.map(this::mapToDTO);
    }

    @Override
    public StudentRequestDTO cancelRequest(Long requestId, Long studentId) {
        log.info("Cancelling request {} by student {}", requestId, studentId);

        // Step 1: Get request and validate
        StudentRequest request = validateRequestExists(requestId);

        // Step 2: Validate ownership
        if (!request.getStudent().getId().equals(studentId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        // Step 3: Validate can be cancelled
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new CustomException(ErrorCode.REQUEST_NOT_PENDING);
        }

        // Step 4: Update status
        request.setStatus(RequestStatus.CANCELLED);
        StudentRequest updated = studentRequestRepository.save(request);

        log.info("Request {} cancelled successfully", requestId);
        return mapToDTO(updated);
    }

    // ==================== VALIDATION HELPER METHODS ====================

    private Student validateStudentExists(Long studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new CustomException(ErrorCode.STUDENT_NOT_FOUND));
    }

    private SessionEntity validateSessionExists(Long sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new CustomException(ErrorCode.SESSION_NOT_FOUND));
    }

    private StudentRequest validateRequestExists(Long requestId) {
        return studentRequestRepository.findById(requestId)
                .orElseThrow(() -> new CustomException(ErrorCode.STUDENT_REQUEST_NOT_FOUND));
    }

    private UserAccount getUserAccount(Long userId) {
        return userAccountRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private void validateRequestIsPending(StudentRequest request) {
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new CustomException(ErrorCode.REQUEST_NOT_PENDING);
        }
    }

    private void validateRequestIsAbsence(StudentRequest request) {
        if (request.getRequestType() != StudentRequestType.ABSENCE) {
            throw new CustomException(ErrorCode.REQUEST_TYPE_MISMATCH);
        }
    }

    private void validateAbsenceRequestRules(Student student, SessionEntity session) {
        log.debug("Validating absence request rules for student {} and session {}", student.getId(), session.getId());

        // Rule 1: Check student is enrolled in the class
        validateStudentEnrolledInClass(student.getId(), session.getClazz().getId());

        // Rule 2: Check session is planned
        validateSessionIsPlanned(session);

        // Rule 3: Check lead time
        validateLeadTime(session.getDate());

        // Rule 4: Check no duplicate pending request
        validateNoDuplicatePendingRequest(student.getId(), session.getId());

        // Rule 5: Check absence quota (warning only, don't block)
        checkAbsenceQuota(student.getId(), session.getClazz().getId());
    }

    private void validateStudentEnrolledInClass(Long studentId, Long classId) {
        boolean isEnrolled = enrollmentRepository.findByStudentIdAndClazzId(studentId, classId)
                .map(enrollment -> enrollment.getStatus() == EnrollmentStatus.ENROLLED)
                .orElse(false);

        if (!isEnrolled) {
            log.warn("Student {} is not enrolled in class {}", studentId, classId);
            throw new CustomException(ErrorCode.STUDENT_NOT_ENROLLED_IN_CLASS);
        }
    }

    private void validateSessionIsPlanned(SessionEntity session) {
        if (session.getStatus() != SessionStatus.PLANNED) {
            log.warn("Session {} is not in planned status (current: {})", session.getId(), session.getStatus());
            throw new CustomException(ErrorCode.SESSION_NOT_PLANNED);
        }

        if (session.getDate().isBefore(LocalDate.now())) {
            log.warn("Session {} has already occurred (date: {})", session.getId(), session.getDate());
            throw new CustomException(ErrorCode.SESSION_ALREADY_OCCURRED);
        }
    }

    private void validateLeadTime(LocalDate sessionDate) {
        LocalDate minimumDate = LocalDate.now().plusDays(REQUEST_LEAD_TIME_DAYS);
        if (sessionDate.isBefore(minimumDate)) {
            log.warn("Session date {} does not meet lead time requirement (minimum: {})", sessionDate, minimumDate);
            throw new CustomException(ErrorCode.ABSENCE_REQUEST_LEAD_TIME_NOT_MET);
        }
    }

    private void validateNoDuplicatePendingRequest(Long studentId, Long sessionId) {
        boolean hasPending = studentRequestRepository.existsPendingAbsenceRequestForSession(studentId, sessionId);
        if (hasPending) {
            log.warn("Student {} already has a pending absence request for session {}", studentId, sessionId);
            throw new CustomException(ErrorCode.DUPLICATE_ABSENCE_REQUEST);
        }
    }

    private void checkAbsenceQuota(Long studentId, Long classId) {
        int approvedCount = studentRequestRepository.countApprovedAbsenceRequestsInClass(studentId, classId);
        if (approvedCount >= MAX_ABSENCE_QUOTA_PER_CLASS) {
            log.warn("Student {} has reached absence quota ({}/{}) in class {}",
                    studentId, approvedCount, MAX_ABSENCE_QUOTA_PER_CLASS, classId);
            // Note: This is a WARNING only, not blocking the request
            // Academic Staff will see this info when reviewing
            // In a production system, you might want to add a flag to the request
        }
    }

    // ==================== BUSINESS LOGIC HELPER METHODS ====================

    private StudentRequest buildAbsenceRequest(Student student, SessionEntity session, CreateAbsenceRequestDTO dto) {
        StudentRequest request = new StudentRequest();
        request.setStudent(student);
        request.setTargetSession(session);
        request.setRequestType(StudentRequestType.ABSENCE);
        request.setStatus(RequestStatus.PENDING);
        request.setNote("Absence reason: " + dto.getReason());
        request.setSubmittedAt(OffsetDateTime.now());
        request.setSubmittedBy(student.getUserAccount());
        request.setCreatedAt(OffsetDateTime.now());
        request.setUpdatedAt(OffsetDateTime.now());

        return request;
    }

    private void updateStudentSessionToExcused(StudentRequest request) {
        Long studentId = request.getStudent().getId();
        Long sessionId = request.getTargetSession().getId();

        log.debug("Updating student_session ({}, {}) to EXCUSED", studentId, sessionId);

        StudentSession studentSession = studentSessionRepository
                .findByIdStudentIdAndIdSessionId(studentId, sessionId)
                .orElseThrow(() -> new CustomException(ErrorCode.STUDENT_SESSION_NOT_FOUND));

        String note = String.format("Approved absence request #%d: %s",
                request.getId(), request.getNote());

        studentSession.setAttendanceStatus(AttendanceStatus.EXCUSED);
        studentSession.setNote(note);

        studentSessionRepository.save(studentSession);
        log.info("Updated student_session ({}, {}) to EXCUSED", studentId, sessionId);
    }

    // ==================== DTO MAPPING METHODS ====================

    private StudentRequestDTO mapToDTO(StudentRequest request) {
        StudentRequestDTO dto = new StudentRequestDTO();
        dto.setId(request.getId());
        dto.setRequestType(request.getRequestType());
        dto.setStatus(request.getStatus());

        // Student information
        if (request.getStudent() != null) {
            dto.setStudentId(request.getStudent().getId());
            if (request.getStudent().getUserAccount() != null) {
                dto.setStudentName(request.getStudent().getUserAccount().getFullName());
                dto.setStudentEmail(request.getStudent().getUserAccount().getEmail());
            }
        }

        // Target session
        if (request.getTargetSession() != null) {
            dto.setTargetSessionId(request.getTargetSession().getId());
            dto.setTargetSession(mapSessionToBasicDTO(request.getTargetSession()));
        }

        // Makeup session (for makeup requests)
        if (request.getMakeupSession() != null) {
            dto.setMakeupSessionId(request.getMakeupSession().getId());
            dto.setMakeupSession(mapSessionToBasicDTO(request.getMakeupSession()));
        }

        // Class information (for transfer requests)
        if (request.getCurrentClass() != null) {
            dto.setCurrentClassId(request.getCurrentClass().getId());
            dto.setCurrentClassName(request.getCurrentClass().getName());
        }
        if (request.getTargetClass() != null) {
            dto.setTargetClassId(request.getTargetClass().getId());
            dto.setTargetClassName(request.getTargetClass().getName());
        }

        // Request details
        dto.setEffectiveDate(request.getEffectiveDate());
        dto.setReason(request.getNote());
        dto.setNote(request.getNote());

        // Submission tracking
        dto.setSubmittedAt(request.getSubmittedAt() != null ? request.getSubmittedAt().toLocalDateTime() : null);
        if (request.getSubmittedBy() != null) {
            dto.setSubmittedBy(request.getSubmittedBy().getId());
            dto.setSubmittedByName(request.getSubmittedBy().getFullName());
        }

        // Decision tracking
        dto.setDecidedAt(request.getDecidedAt() != null ? request.getDecidedAt().toLocalDateTime() : null);
        if (request.getDecidedBy() != null) {
            dto.setDecidedBy(request.getDecidedBy().getId());
            dto.setDecidedByName(request.getDecidedBy().getFullName());
        }

        return dto;
    }

    private SessionBasicDTO mapSessionToBasicDTO(SessionEntity session) {
        SessionBasicDTO dto = new SessionBasicDTO();
        dto.setId(session.getId());
        dto.setDate(session.getDate());
        dto.setStartTime(session.getStartTime());
        dto.setEndTime(session.getEndTime());
        dto.setStatus(session.getStatus());

        if (session.getCourseSession() != null) {
            dto.setTopic(session.getCourseSession().getTopic());
            dto.setSequenceNo(session.getCourseSession().getSequenceNumber());
        }

        if (session.getClazz() != null) {
            dto.setClassName(session.getClazz().getName());
            if (session.getClazz().getBranch() != null) {
                dto.setBranchName(session.getClazz().getBranch().getName());
            }
        }

        return dto;
    }
}
