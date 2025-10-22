package org.fyp.emssep490be.services.enrollment.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fyp.emssep490be.dtos.enrollment.*;
import org.fyp.emssep490be.entities.*;
import org.fyp.emssep490be.entities.enums.AttendanceStatus;
import org.fyp.emssep490be.entities.enums.ClassStatus;
import org.fyp.emssep490be.entities.enums.EnrollmentStatus;
import org.fyp.emssep490be.entities.enums.SessionStatus;
import org.fyp.emssep490be.entities.ids.StudentSessionId;
import org.fyp.emssep490be.exceptions.CustomException;
import org.fyp.emssep490be.exceptions.ErrorCode;
import org.fyp.emssep490be.repositories.*;
import org.fyp.emssep490be.services.enrollment.EnrollmentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of EnrollmentService
 * Handles student enrollment with automatic StudentSession generation (session-first pattern)
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final ClassRepository classRepository;
    private final SessionRepository sessionRepository;
    private final StudentSessionRepository studentSessionRepository;

    @Override
    public BatchEnrollResponseDTO batchEnrollStudents(Long classId, List<Long> studentIds) {
        log.info("Batch enrolling {} students to class ID: {}", studentIds.size(), classId);

        BatchEnrollResponseDTO response = BatchEnrollResponseDTO.builder()
                .totalRequested(studentIds.size())
                .successfulCount(0)
                .failedCount(0)
                .successful(new ArrayList<>())
                .failed(new ArrayList<>())
                .build();

        // Validate class exists and is available for enrollment
        ClassEntity clazz = classRepository.findById(classId)
                .orElseThrow(() -> new CustomException(ErrorCode.CLASS_NOT_FOUND));

        // Check if class status allows enrollment
        if (clazz.getStatus() != ClassStatus.SCHEDULED && clazz.getStatus() != ClassStatus.ONGOING) {
            throw new CustomException(ErrorCode.CLASS_NOT_AVAILABLE);
        }

        // Get current enrollment count
        long currentEnrolled = enrollmentRepository.countByClazzId(classId);

        // Get all future/planned sessions for this class
        List<SessionEntity> futureSessions = sessionRepository.findByClazzIdAndDateAfter(
                classId, 
                LocalDate.now().minusDays(1) // Include today's sessions
        );

        log.debug("Found {} future sessions for class ID: {}", futureSessions.size(), classId);

        // Process each student individually (partial success pattern)
        for (Long studentId : studentIds) {
            try {
                // Check capacity before each enrollment
                if (currentEnrolled >= clazz.getMaxCapacity()) {
                    Student student = studentRepository.findById(studentId).orElse(null);
                    response.getFailed().add(BatchEnrollResponseDTO.EnrollmentError.builder()
                            .studentId(studentId)
                            .studentCode(student != null ? student.getStudentCode() : null)
                            .studentName(student != null ? student.getUserAccount().getFullName() : null)
                            .reason("Class capacity exceeded (max: " + clazz.getMaxCapacity() + ")")
                            .build());
                    response.setFailedCount(response.getFailedCount() + 1);
                    continue;
                }

                // Validate student exists
                Student student = studentRepository.findById(studentId)
                        .orElseThrow(() -> new CustomException(ErrorCode.STUDENT_NOT_FOUND));

                // Check if already enrolled
                boolean alreadyEnrolled = enrollmentRepository.existsByStudentIdAndClazzId(studentId, classId);
                if (alreadyEnrolled) {
                    response.getFailed().add(BatchEnrollResponseDTO.EnrollmentError.builder()
                            .studentId(studentId)
                            .studentCode(student.getStudentCode())
                            .studentName(student.getUserAccount().getFullName())
                            .reason("Student is already enrolled in this class")
                            .build());
                    response.setFailedCount(response.getFailedCount() + 1);
                    continue;
                }

                // Create enrollment record
                Enrollment enrollment = new Enrollment();
                enrollment.setStudent(student);
                enrollment.setClazz(clazz);
                enrollment.setStatus(EnrollmentStatus.ENROLLED);
                enrollment.setEnrolledAt(OffsetDateTime.now());

                enrollment = enrollmentRepository.save(enrollment);
                log.info("Created enrollment ID: {} for student ID: {} in class ID: {}", 
                        enrollment.getId(), studentId, classId);

                // Auto-generate StudentSession records for all future/planned sessions
                List<StudentSession> studentSessions = futureSessions.stream()
                        .map(session -> {
                            StudentSession ss = new StudentSession();
                            StudentSessionId id = new StudentSessionId(studentId, session.getId());
                            ss.setId(id);
                            ss.setStudent(student);
                            ss.setSession(session);
                            ss.setAttendanceStatus(AttendanceStatus.PLANNED);
                            ss.setIsMakeup(false);
                            return ss;
                        })
                        .collect(Collectors.toList());

                studentSessionRepository.saveAll(studentSessions);
                log.debug("Created {} StudentSession records for student ID: {}", studentSessions.size(), studentId);

                // Add to successful list
                response.getSuccessful().add(EnrollmentResponseDTO.builder()
                        .enrollmentId(enrollment.getId())
                        .classId(classId)
                        .studentId(studentId)
                        .enrollmentStatus(enrollment.getStatus().name())
                        .enrolledAt(enrollment.getEnrolledAt().toLocalDateTime())
                        .build());

                response.setSuccessfulCount(response.getSuccessfulCount() + 1);
                currentEnrolled++;

            } catch (CustomException e) {
                log.warn("Failed to enroll student ID: {} - {}", studentId, e.getMessage());
                response.getFailed().add(BatchEnrollResponseDTO.EnrollmentError.builder()
                        .studentId(studentId)
                        .reason(e.getMessage())
                        .build());
                response.setFailedCount(response.getFailedCount() + 1);
            } catch (Exception e) {
                log.error("Unexpected error enrolling student ID: {}", studentId, e);
                response.getFailed().add(BatchEnrollResponseDTO.EnrollmentError.builder()
                        .studentId(studentId)
                        .reason("Internal error: " + e.getMessage())
                        .build());
                response.setFailedCount(response.getFailedCount() + 1);
            }
        }

        log.info("Batch enrollment completed: {}/{} successful", 
                response.getSuccessfulCount(), response.getTotalRequested());

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EnrollmentDetailDTO> getClassEnrollments(Long classId, String status, Pageable pageable) {
        log.info("Getting enrollments for class ID: {} with status: {}", classId, status);

        // Validate class exists
        ClassEntity clazz = classRepository.findById(classId)
                .orElseThrow(() -> new CustomException(ErrorCode.CLASS_NOT_FOUND));

        // Get enrollments with filter
        Page<Enrollment> enrollments;
        if (status != null && !status.isEmpty()) {
            EnrollmentStatus enrollmentStatus = EnrollmentStatus.valueOf(status);
            enrollments = enrollmentRepository.findAll(
                    (root, query, cb) -> cb.and(
                            cb.equal(root.get("clazz").get("id"), classId),
                            cb.equal(root.get("status"), enrollmentStatus)
                    ),
                    pageable
            );
        } else {
            enrollments = enrollmentRepository.findAll(
                    (root, query, cb) -> cb.equal(root.get("clazz").get("id"), classId),
                    pageable
            );
        }

        // Map to DTOs
        List<EnrollmentDetailDTO> dtos = enrollments.getContent().stream()
                .map(this::mapToEnrollmentDetailDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, enrollments.getTotalElements());
    }

    @Override
    @Transactional // Explicit transaction for write operations (update + delete)
    public void removeStudentFromClass(Long classId, Long studentId) {
        log.info("Removing student ID: {} from class ID: {}", studentId, classId);

        // Find enrollment
        Enrollment enrollment = enrollmentRepository.findByStudentIdAndClazzId(studentId, classId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENROLLMENT_NOT_FOUND));

        // Check if class is completed
        if (enrollment.getStatus() == EnrollmentStatus.COMPLETED) {
            throw new CustomException(ErrorCode.CANNOT_UNENROLL_COMPLETED_CLASS);
        }

        // Update enrollment status to dropped
        enrollment.setStatus(EnrollmentStatus.DROPPED);
        enrollment.setLeftAt(OffsetDateTime.now());
        enrollmentRepository.save(enrollment);

        log.info("Updated enrollment ID: {} status to DROPPED", enrollment.getId());

        // Remove future StudentSession records (keep past sessions for audit)
        List<StudentSession> futureSessions = studentSessionRepository.findAll(
                (root, query, cb) -> cb.and(
                        cb.equal(root.get("id").get("studentId"), studentId),
                        cb.equal(root.get("session").get("clazz").get("id"), classId),
                        cb.greaterThanOrEqualTo(root.get("session").get("date"), LocalDate.now()),
                        cb.equal(root.get("attendanceStatus"), AttendanceStatus.PLANNED)
                )
        );

        if (!futureSessions.isEmpty()) {
            studentSessionRepository.deleteAll(futureSessions);
            log.info("Deleted {} future StudentSession records for student ID: {}", 
                    futureSessions.size(), studentId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentDetailDTO> getStudentEnrollments(Long studentId, List<String> statusList) {
        log.info("Getting enrollments for student ID: {} with statuses: {}", studentId, statusList);

        // Validate student exists
        studentRepository.findById(studentId)
                .orElseThrow(() -> new CustomException(ErrorCode.STUDENT_NOT_FOUND));

        // Get enrollments
        List<Enrollment> enrollments;
        if (statusList != null && !statusList.isEmpty()) {
            List<EnrollmentStatus> statuses = statusList.stream()
                    .map(EnrollmentStatus::valueOf)
                    .collect(Collectors.toList());
            enrollments = enrollmentRepository.findAll(
                    (root, query, cb) -> cb.and(
                            cb.equal(root.get("student").get("id"), studentId),
                            root.get("status").in(statuses)
                    )
            );
        } else {
            enrollments = enrollmentRepository.findByStudentId(studentId);
        }

        return enrollments.stream()
                .map(this::mapToEnrollmentDetailDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AvailableStudentDTO> getAvailableStudentsForClass(Long classId, String search, Pageable pageable) {
        log.info("Getting available students for class ID: {} with search: {}", classId, search);

        // Validate class exists
        classRepository.findById(classId)
                .orElseThrow(() -> new CustomException(ErrorCode.CLASS_NOT_FOUND));

        // Get all students with search filter
        Page<Student> students;
        if (search != null && !search.isEmpty()) {
            String searchPattern = "%" + search.toLowerCase() + "%";
            students = studentRepository.findAll(
                    (root, query, cb) -> cb.or(
                            cb.like(cb.lower(root.get("studentCode")), searchPattern),
                            cb.like(cb.lower(root.get("userAccount").get("fullName")), searchPattern),
                            cb.like(cb.lower(root.get("userAccount").get("email")), searchPattern)
                    ),
                    pageable
            );
        } else {
            students = studentRepository.findAll(pageable);
        }

        // Get enrolled student IDs for this class
        List<Long> enrolledStudentIds = enrollmentRepository.findByClazzId(classId).stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.ENROLLED || 
                            e.getStatus() == EnrollmentStatus.WAITLISTED)
                .map(e -> e.getStudent().getId())
                .collect(Collectors.toList());

        // Map to DTOs with enrollment status
        List<AvailableStudentDTO> dtos = students.getContent().stream()
                .map(student -> AvailableStudentDTO.builder()
                        .studentId(student.getId())
                        .studentCode(student.getStudentCode())
                        .fullName(student.getUserAccount().getFullName())
                        .email(student.getUserAccount().getEmail())
                        .phone(student.getUserAccount().getPhone())
                        .branchName(student.getBranch() != null ? student.getBranch().getName() : null)
                        .isEnrolled(enrolledStudentIds.contains(student.getId()))
                        .build())
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, students.getTotalElements());
    }

    /**
     * Helper method to map Enrollment entity to EnrollmentDetailDTO
     */
    private EnrollmentDetailDTO mapToEnrollmentDetailDTO(Enrollment enrollment) {
        Student student = enrollment.getStudent();
        ClassEntity clazz = enrollment.getClazz();
        UserAccount userAccount = student.getUserAccount();

        return EnrollmentDetailDTO.builder()
                .enrollmentId(enrollment.getId())
                .studentId(student.getId())
                .studentCode(student.getStudentCode())
                .studentFullName(userAccount.getFullName())
                .studentEmail(userAccount.getEmail())
                .studentPhone(userAccount.getPhone())
                .classId(clazz.getId())
                .classCode(clazz.getCode())
                .className(clazz.getName())
                .courseCode(clazz.getCourse() != null ? clazz.getCourse().getCode() : null)
                .courseName(clazz.getCourse() != null ? clazz.getCourse().getName() : null)
                .branchName(clazz.getBranch() != null ? clazz.getBranch().getName() : null)
                .enrollmentStatus(enrollment.getStatus().name())
                .enrolledAt(enrollment.getEnrolledAt().toLocalDateTime())
                .completedAt(null) // No completedAt field in Enrollment entity
                .leftAt(enrollment.getLeftAt() != null ? enrollment.getLeftAt().toLocalDateTime() : null)
                .build();
    }
}
