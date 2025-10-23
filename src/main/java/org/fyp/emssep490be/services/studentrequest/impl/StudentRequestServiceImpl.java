package org.fyp.emssep490be.services.studentrequest.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fyp.emssep490be.dtos.studentrequest.*;
import org.fyp.emssep490be.entities.*;
import org.fyp.emssep490be.entities.enums.*;
import org.fyp.emssep490be.entities.ids.StudentSessionId;
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
    private static final int MAX_MAKEUP_QUOTA_PER_CLASS = 5;

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

        // Step 1: Get request with pessimistic lock and validate
        StudentRequest request = studentRequestRepository.findByIdWithLock(requestId)
                .orElseThrow(() -> new CustomException(ErrorCode.STUDENT_REQUEST_NOT_FOUND));
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

        // Step 1: Get request with pessimistic lock and validate
        StudentRequest request = studentRequestRepository.findByIdWithLock(requestId)
                .orElseThrow(() -> new CustomException(ErrorCode.STUDENT_REQUEST_NOT_FOUND));
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

        // Step 1: Get request with pessimistic lock and validate
        StudentRequest request = studentRequestRepository.findByIdWithLock(requestId)
                .orElseThrow(() -> new CustomException(ErrorCode.STUDENT_REQUEST_NOT_FOUND));

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

    // ==================== MAKEUP REQUEST OPERATIONS ====================

    @Override
    @Transactional(readOnly = true)
    public MakeupSessionSearchResultDTO findAvailableMakeupSessions(
            Long studentId,
            Long missedSessionId,
            LocalDate dateFrom,
            LocalDate dateTo,
            Long branchId,
            Modality modality
    ) {
        log.info("Finding available makeup sessions for student {} and missed session {}", studentId, missedSessionId);

        // 1. Validate student exists
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new CustomException(ErrorCode.STUDENT_NOT_FOUND));

        // 2. Validate missed session exists
        SessionEntity missedSession = sessionRepository.findById(missedSessionId)
                .orElseThrow(() -> new CustomException(ErrorCode.SESSION_NOT_FOUND));

        // 3. Validate student has this session (StudentSession exists)
        StudentSession studentSession = studentSessionRepository
                .findByIdStudentIdAndIdSessionId(studentId, missedSessionId)
                .orElseThrow(() -> new CustomException(ErrorCode.STUDENT_SESSION_NOT_FOUND));

        // 4. Get course_session_id to find matching sessions
        Long courseSessionId = missedSession.getCourseSession().getId();
        log.debug("Looking for makeup sessions with course_session_id: {}", courseSessionId);

        // 5. Query available makeup sessions
        List<Object[]> results = sessionRepository.findAvailableMakeupSessions(
                courseSessionId,
                studentId,
                dateFrom,
                dateTo,
                branchId,
                modality
        );

        // 6. Map results to DTOs
        List<AvailableMakeupSessionDTO> makeupSessions = results.stream()
                .map(this::mapToAvailableMakeupSessionDTO)
                .collect(Collectors.toList());

        log.info("Found {} available makeup sessions", makeupSessions.size());

        // 7. Build response with target session info
        SessionBasicDTO targetSessionInfo = mapSessionToBasicDTO(missedSession);

        return MakeupSessionSearchResultDTO.builder()
                .total(makeupSessions.size())
                .makeupSessions(makeupSessions)
                .targetSessionInfo(targetSessionInfo)
                .build();
    }

    @Override
    public StudentRequestDTO createMakeupRequest(Long studentId, CreateMakeupRequestDTO request) {
        log.info("Creating makeup request for student {} - target session: {}, makeup session: {}",
                studentId, request.getTargetSessionId(), request.getMakeupSessionId());

        // 1. Validate student exists
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new CustomException(ErrorCode.STUDENT_NOT_FOUND));

        // 2. Validate target session exists
        SessionEntity targetSession = sessionRepository.findById(request.getTargetSessionId())
                .orElseThrow(() -> new CustomException(ErrorCode.SESSION_NOT_FOUND));

        // 3. Validate makeup session exists
        SessionEntity makeupSession = sessionRepository.findById(request.getMakeupSessionId())
                .orElseThrow(() -> new CustomException(ErrorCode.SESSION_NOT_FOUND));

        // 4. Validate student has target session
        StudentSession targetStudentSession = studentSessionRepository
                .findByIdStudentIdAndIdSessionId(studentId, request.getTargetSessionId())
                .orElseThrow(() -> new CustomException(ErrorCode.STUDENT_SESSION_NOT_FOUND));

        // 5. Validate target session attendance status (must be ABSENT or PLANNED)
        if (targetStudentSession.getAttendanceStatus() != AttendanceStatus.ABSENT &&
            targetStudentSession.getAttendanceStatus() != AttendanceStatus.PLANNED) {
            throw new CustomException(ErrorCode.INVALID_ATTENDANCE_STATUS_FOR_MAKEUP);
        }

        // 6. Validate both sessions have same course_session_id ⭐ KEY RULE
        if (!targetSession.getCourseSession().getId().equals(makeupSession.getCourseSession().getId())) {
            log.error("Course session mismatch - target: {}, makeup: {}",
                    targetSession.getCourseSession().getId(), makeupSession.getCourseSession().getId());
            throw new CustomException(ErrorCode.MAKEUP_COURSE_SESSION_MISMATCH);
        }

        // 7. Validate makeup session status = PLANNED
        if (makeupSession.getStatus() != SessionStatus.PLANNED) {
            throw new CustomException(ErrorCode.SESSION_NOT_PLANNED);
        }

        // 8. Validate makeup session date >= today (future)
        if (makeupSession.getDate().isBefore(LocalDate.now())) {
            throw new CustomException(ErrorCode.SESSION_ALREADY_OCCURRED);
        }

        // 9. Validate student not already enrolled in makeup session
        if (studentSessionRepository.findByIdStudentIdAndIdSessionId(studentId, request.getMakeupSessionId()).isPresent()) {
            throw new CustomException(ErrorCode.STUDENT_ALREADY_ENROLLED_IN_MAKEUP);
        }

        // 10. Validate capacity available
        long enrolledCount = sessionRepository.countEnrolledStudents(request.getMakeupSessionId());
        Integer maxCapacity = makeupSession.getClazz().getMaxCapacity();
        if (enrolledCount >= maxCapacity) {
            log.error("Makeup session {} is full ({}/{})", request.getMakeupSessionId(), enrolledCount, maxCapacity);
            throw new CustomException(ErrorCode.MAKEUP_SESSION_CAPACITY_FULL);
        }

        // 11. Validate no schedule conflict
        long conflicts = studentSessionRepository.countScheduleConflicts(
                studentId,
                makeupSession.getDate(),
                makeupSession.getStartTime(),
                makeupSession.getEndTime()
        );
        if (conflicts > 0) {
            log.error("Schedule conflict detected for student {} on {} at {}-{}",
                    studentId, makeupSession.getDate(), makeupSession.getStartTime(), makeupSession.getEndTime());
            throw new CustomException(ErrorCode.SCHEDULE_CONFLICT);
        }

        // 12. Check makeup quota (warning or blocking based on business rule)
        Long classId = targetSession.getClazz().getId();
        long makeupCount = studentSessionRepository.countMakeupSessions(studentId, classId);
        if (makeupCount >= MAX_MAKEUP_QUOTA_PER_CLASS) {
            log.warn("Student {} has reached makeup quota ({}/{}) in class {}",
                    studentId, makeupCount, MAX_MAKEUP_QUOTA_PER_CLASS, classId);
            throw new CustomException(ErrorCode.MAKEUP_QUOTA_EXCEEDED);
        }

        // 13. Check for duplicate pending request
        boolean hasPendingRequest = studentRequestRepository.existsPendingMakeupRequest(
                studentId, request.getTargetSessionId(), request.getMakeupSessionId()
        );
        if (hasPendingRequest) {
            throw new CustomException(ErrorCode.DUPLICATE_ABSENCE_REQUEST); // Reuse error code
        }

        // 14. Create StudentRequest entity
        StudentRequest studentRequest = new StudentRequest();
        studentRequest.setStudent(student);
        studentRequest.setRequestType(StudentRequestType.MAKEUP);
        studentRequest.setTargetSession(targetSession);
        studentRequest.setMakeupSession(makeupSession);
        studentRequest.setCurrentClass(targetSession.getClazz());
        studentRequest.setStatus(RequestStatus.PENDING);
        studentRequest.setNote(request.getReason());
        studentRequest.setSubmittedAt(OffsetDateTime.now());
        studentRequest.setSubmittedBy(student.getUserAccount());

        // 15. Save request
        StudentRequest savedRequest = studentRequestRepository.save(studentRequest);
        log.info("Makeup request created successfully with ID: {}", savedRequest.getId());

        return mapToDTO(savedRequest);
    }

    @Override
    public StudentRequestDTO approveMakeupRequest(Long requestId, Long staffId, ApproveRequestDTO dto) {
        log.info("Approving makeup request {} by staff {}", requestId, staffId);

        // 1. Validate request exists - WITH PESSIMISTIC LOCK to prevent concurrent approvals
        StudentRequest request = studentRequestRepository.findByIdWithLock(requestId)
                .orElseThrow(() -> new CustomException(ErrorCode.STUDENT_REQUEST_NOT_FOUND));

        // 2. Validate status = PENDING
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new CustomException(ErrorCode.REQUEST_NOT_PENDING);
        }

        // 3. Validate requestType = MAKEUP
        if (request.getRequestType() != StudentRequestType.MAKEUP) {
            throw new CustomException(ErrorCode.REQUEST_TYPE_MISMATCH);
        }

        // 4. Get staff account
        UserAccount staffAccount = userAccountRepository.findById(staffId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 5. Re-validate capacity WITH LOCK (may have changed since submission) - RACE CONDITION PREVENTION
        SessionEntity makeupSession = sessionRepository.findByIdWithLock(request.getMakeupSession().getId())
                .orElseThrow(() -> new CustomException(ErrorCode.SESSION_NOT_FOUND));
        long currentEnrolledCount = sessionRepository.countEnrolledStudents(makeupSession.getId());
        Integer maxCapacity = makeupSession.getClazz().getMaxCapacity();
        if (currentEnrolledCount >= maxCapacity) {
            log.error("Makeup session {} is now full ({}/{})", makeupSession.getId(), currentEnrolledCount, maxCapacity);
            throw new CustomException(ErrorCode.MAKEUP_SESSION_NOW_FULL);
        }

        // 6. Re-validate no schedule conflict - RACE CONDITION PREVENTION
        long conflicts = studentSessionRepository.countScheduleConflicts(
                request.getStudent().getId(),
                makeupSession.getDate(),
                makeupSession.getStartTime(),
                makeupSession.getEndTime()
        );
        if (conflicts > 0) {
            throw new CustomException(ErrorCode.SCHEDULE_CONFLICT);
        }

        // TRANSACTION: Update original session + Create new makeup session

        // 7. Update original StudentSession to EXCUSED
        SessionEntity targetSession = request.getTargetSession();
        StudentSession targetStudentSession = studentSessionRepository
                .findByIdStudentIdAndIdSessionId(request.getStudent().getId(), targetSession.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.STUDENT_SESSION_NOT_FOUND));

        targetStudentSession.setAttendanceStatus(AttendanceStatus.EXCUSED);
        targetStudentSession.setNote(String.format("Approved makeup request #%d: Will attend makeup session %d (Class: %s, Date: %s)",
                request.getId(), makeupSession.getId(), makeupSession.getClazz().getName(), makeupSession.getDate()));
        studentSessionRepository.save(targetStudentSession);

        log.debug("Updated target student_session ({}, {}) to EXCUSED",
                request.getStudent().getId(), targetSession.getId());

        // 8. Create NEW StudentSession for makeup with is_makeup = TRUE
        StudentSessionId makeupId = new StudentSessionId(request.getStudent().getId(), makeupSession.getId());
        StudentSession makeupStudentSession = new StudentSession();
        makeupStudentSession.setId(makeupId);
        makeupStudentSession.setStudent(request.getStudent());
        makeupStudentSession.setSession(makeupSession);
        makeupStudentSession.setIsMakeup(true);
        makeupStudentSession.setAttendanceStatus(AttendanceStatus.PLANNED);
        makeupStudentSession.setNote(String.format("Makeup for session %d (Class: %s, Date: %s) - Request #%d",
                targetSession.getId(), targetSession.getClazz().getName(), targetSession.getDate(), request.getId()));
        studentSessionRepository.save(makeupStudentSession);

        log.debug("Created makeup student_session ({}, {}) with is_makeup=true",
                request.getStudent().getId(), makeupSession.getId());

        // 9. Update request status
        request.setStatus(RequestStatus.APPROVED);
        request.setDecidedBy(staffAccount);
        request.setDecidedAt(OffsetDateTime.now());
        if (dto.getDecisionNotes() != null && !dto.getDecisionNotes().isBlank()) {
            request.setNote(request.getNote() + "\n\nStaff decision: " + dto.getDecisionNotes());
        }

        StudentRequest updatedRequest = studentRequestRepository.save(request);
        log.info("Makeup request {} approved successfully", requestId);

        return mapToDTO(updatedRequest);
    }

    // ==================== HELPER METHODS FOR MAKEUP ====================

    /**
     * Map query result (Object[]) to AvailableMakeupSessionDTO
     * Query returns: [SessionEntity, ClassEntity, Branch, CourseSession, availableSlots, enrolledCount]
     */
    private AvailableMakeupSessionDTO mapToAvailableMakeupSessionDTO(Object[] row) {
        SessionEntity session = (SessionEntity) row[0];
        ClassEntity classEntity = (ClassEntity) row[1];
        Branch branch = (Branch) row[2];
        CourseSession courseSession = (CourseSession) row[3];
        Long availableSlots = (Long) row[4];
        Long enrolledCount = (Long) row[5];

        return AvailableMakeupSessionDTO.builder()
                .sessionId(session.getId())
                .classId(classEntity.getId())
                .className(classEntity.getName())
                .branchId(branch.getId())
                .branchName(branch.getName())
                .modality(classEntity.getModality())
                .date(session.getDate())
                .startTime(session.getStartTime())
                .endTime(session.getEndTime())
                .courseSessionId(courseSession.getId())
                .topic(courseSession.getTopic())
                .sequenceNo(courseSession.getSequenceNumber())
                .availableSlots(availableSlots.intValue())
                .maxCapacity(classEntity.getMaxCapacity())
                .enrolledCount(enrolledCount.intValue())
                .build();
    }
}
