package org.fyp.emssep490be.services.course.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fyp.emssep490be.configs.CustomUserDetails;
import org.fyp.emssep490be.dtos.common.PagedResponseDTO;
import org.fyp.emssep490be.dtos.common.PaginationDTO;
import org.fyp.emssep490be.dtos.course.*;
import org.fyp.emssep490be.dtos.coursephase.CoursePhaseDTO;
import org.fyp.emssep490be.entities.Course;
import org.fyp.emssep490be.entities.CoursePhase;
import org.fyp.emssep490be.entities.Level;
import org.fyp.emssep490be.entities.Subject;
import org.fyp.emssep490be.entities.UserAccount;
import org.fyp.emssep490be.exceptions.CustomException;
import org.fyp.emssep490be.exceptions.ErrorCode;
import org.fyp.emssep490be.repositories.ClassRepository;
import org.fyp.emssep490be.repositories.CoursePhaseRepository;
import org.fyp.emssep490be.repositories.CourseRepository;
import org.fyp.emssep490be.repositories.LevelRepository;
import org.fyp.emssep490be.repositories.SubjectRepository;
import org.fyp.emssep490be.repositories.UserAccountRepository;
import org.fyp.emssep490be.services.course.CourseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of CourseService for Course management operations
 * Handles CRUD operations and approval workflow for courses
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final SubjectRepository subjectRepository;
    private final LevelRepository levelRepository;
    private final CoursePhaseRepository coursePhaseRepository;
    private final ClassRepository classRepository;
    private final UserAccountRepository userAccountRepository;

    /**
     * Get all courses with pagination and filtering
     */
    @Override
    @Transactional(readOnly = true)
    public PagedResponseDTO<CourseDTO> getAllCourses(Long subjectId, Long levelId, String status, Boolean approved, Integer page, Integer limit) {
        log.info("Getting all courses: subjectId={}, levelId={}, status={}, approved={}, page={}, limit={}",
                 subjectId, levelId, status, approved, page, limit);

        // Set default pagination values
        int pageNumber = (page != null && page > 0) ? page - 1 : 0;
        int pageSize = (limit != null && limit > 0) ? limit : 10;

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("id").descending());

        // Use repository filter method
        Page<Course> coursePage = courseRepository.findByFilters(subjectId, levelId, status, approved, pageable);

        List<CourseDTO> courseDTOs = coursePage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        PaginationDTO pagination = new PaginationDTO(
                coursePage.getNumber() + 1,
                coursePage.getTotalPages(),
                coursePage.getTotalElements(),
                coursePage.getSize()
        );

        log.info("Retrieved {} courses", courseDTOs.size());
        return new PagedResponseDTO<>(courseDTOs, pagination);
    }

    /**
     * Get course by ID with detailed information
     */
    @Override
    @Transactional(readOnly = true)
    public CourseDetailDTO getCourseById(Long id) {
        log.info("Getting course by ID: {}", id);

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Course not found with ID: {}", id);
                    return new CustomException(ErrorCode.COURSE_NOT_FOUND);
                });

        CourseDetailDTO detailDTO = convertToDetailDTO(course);
        log.info("Retrieved course: {}", course.getCode());
        return detailDTO;
    }

    /**
     * Create a new course
     */
    @Override
    public CourseDTO createCourse(CreateCourseRequestDTO request) {
        log.info("Creating course: {}", request.getCode());

        // Validate subject exists
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> {
                    log.error("Subject not found with ID: {}", request.getSubjectId());
                    return new CustomException(ErrorCode.SUBJECT_NOT_FOUND);
                });

        // Validate level exists
        Level level = levelRepository.findById(request.getLevelId())
                .orElseThrow(() -> {
                    log.error("Level not found with ID: {}", request.getLevelId());
                    return new CustomException(ErrorCode.LEVEL_NOT_FOUND);
                });

        // Check unique code
        if (courseRepository.existsByCode(request.getCode())) {
            log.error("Course code already exists: {}", request.getCode());
            throw new CustomException(ErrorCode.COURSE_CODE_DUPLICATE);
        }

        // Validate total hours consistency (tolerance check)
        if (request.getTotalHours() != null && request.getDurationWeeks() != null &&
            request.getSessionPerWeek() != null && request.getHoursPerSession() != null) {
            double calculated = request.getDurationWeeks() * request.getSessionPerWeek() * request.getHoursPerSession();
            double tolerance = 0.1; // 10% tolerance
            if (Math.abs(request.getTotalHours() - calculated) / calculated > tolerance) {
                log.error("Total hours mismatch: provided={}, calculated={}", request.getTotalHours(), calculated);
                throw new CustomException(ErrorCode.INVALID_TOTAL_HOURS);
            }
        }

        // Get current user
        UserAccount currentUser = getCurrentUser();

        // Build Course entity
        Course course = new Course();
        course.setSubject(subject);
        course.setLevel(level);
        course.setCode(request.getCode());
        course.setName(request.getName());
        course.setVersion(request.getVersion());
        course.setDescription(request.getDescription());
        course.setTotalHours(request.getTotalHours() != null ? request.getTotalHours().intValue() : null);
        course.setDurationWeeks(request.getDurationWeeks());
        course.setSessionPerWeek(request.getSessionPerWeek());
        course.setHoursPerSession(request.getHoursPerSession() != null ? BigDecimal.valueOf(request.getHoursPerSession()) : null);
        course.setPrerequisites(request.getPrerequisites());
        course.setTargetAudience(request.getTargetAudience());
        course.setTeachingMethods(request.getTeachingMethods());
        course.setEffectiveDate(request.getEffectiveDate());
        course.setStatus(request.getStatus() != null ? request.getStatus() : "draft");
        course.setCreatedBy(currentUser);
        course.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        course.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        // Calculate hash checksum
        String hashChecksum = calculateHashChecksum(course);
        course.setHashChecksum(hashChecksum);

        Course savedCourse = courseRepository.save(course);
        log.info("Course created successfully with ID: {}", savedCourse.getId());

        return convertToDTO(savedCourse);
    }

    /**
     * Update an existing course
     */
    @Override
    public CourseDTO updateCourse(Long id, UpdateCourseRequestDTO request) {
        log.info("Updating course ID: {}", id);

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Course not found with ID: {}", id);
                    return new CustomException(ErrorCode.COURSE_NOT_FOUND);
                });

        // Check if course can be updated (must be draft or rejected)
        if (!"draft".equalsIgnoreCase(course.getStatus()) && course.getApprovedByManager() != null) {
            log.error("Course cannot be updated - status: {}, approved: {}", course.getStatus(), course.getApprovedByManager() != null);
            throw new CustomException(ErrorCode.COURSE_CANNOT_BE_UPDATED);
        }

        // Update fields
        if (request.getName() != null) {
            course.setName(request.getName());
        }
        if (request.getDescription() != null) {
            course.setDescription(request.getDescription());
        }
        if (request.getPrerequisites() != null) {
            course.setPrerequisites(request.getPrerequisites());
        }
        if (request.getTargetAudience() != null) {
            course.setTargetAudience(request.getTargetAudience());
        }
        if (request.getTeachingMethods() != null) {
            course.setTeachingMethods(request.getTeachingMethods());
        }
        if (request.getStatus() != null) {
            course.setStatus(request.getStatus());
        }

        course.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        // Recalculate hash checksum
        String hashChecksum = calculateHashChecksum(course);
        course.setHashChecksum(hashChecksum);

        Course updatedCourse = courseRepository.save(course);
        log.info("Course updated successfully: {}", updatedCourse.getCode());

        return convertToDTO(updatedCourse);
    }

    /**
     * Submit course for approval
     */
    @Override
    public CourseDTO submitCourseForApproval(Long id) {
        log.info("Submitting course ID: {} for approval", id);

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Course not found with ID: {}", id);
                    return new CustomException(ErrorCode.COURSE_NOT_FOUND);
                });

        // Check if already submitted
        if (course.getApprovedAt() != null || course.getApprovedByManager() != null) {
            log.error("Course already submitted/approved: {}", course.getCode());
            throw new CustomException(ErrorCode.COURSE_ALREADY_SUBMITTED);
        }

        // Check status
        if (!"draft".equalsIgnoreCase(course.getStatus())) {
            log.error("Course must be in draft status to submit: {}", course.getStatus());
            throw new CustomException(ErrorCode.COURSE_CANNOT_BE_MODIFIED);
        }

        // Check if has at least one phase
        long phaseCount = coursePhaseRepository.countByCourseId(id);
        if (phaseCount == 0) {
            log.error("Course must have at least one phase before submission: {}", course.getCode());
            throw new CustomException(ErrorCode.COURSE_NO_PHASES);
        }

        // Note: We don't change status here, just mark as submitted
        // Status changes happen during approval/rejection
        course.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        Course updatedCourse = courseRepository.save(course);
        log.info("Course submitted for approval: {}", updatedCourse.getCode());

        return convertToDTO(updatedCourse);
    }

    /**
     * Approve or reject a course
     */
    @Override
    public CourseDTO approveCourse(Long id, ApprovalRequestDTO request) {
        log.info("Processing approval for course ID: {}, action: {}", id, request.getAction());

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Course not found with ID: {}", id);
                    return new CustomException(ErrorCode.COURSE_NOT_FOUND);
                });

        // Validate action
        String action = request.getAction().toLowerCase();
        if (!"approve".equals(action) && !"reject".equals(action)) {
            log.error("Invalid action: {}", request.getAction());
            throw new CustomException(ErrorCode.INVALID_ACTION);
        }

        // Get current user
        UserAccount currentUser = getCurrentUser();

        if ("approve".equals(action)) {
            // Approve course
            course.setApprovedByManager(currentUser);
            course.setApprovedAt(OffsetDateTime.now(ZoneOffset.UTC));
            course.setStatus("active");
            course.setRejectionReason(null); // Clear rejection reason
            log.info("Course approved by user ID: {}", currentUser.getId());
        } else {
            // Reject course
            if (request.getRejectionReason() == null || request.getRejectionReason().isBlank()) {
                log.error("Rejection reason is required");
                throw new CustomException(ErrorCode.REJECTION_REASON_REQUIRED);
            }

            course.setRejectionReason(request.getRejectionReason());
            course.setStatus("draft"); // Allow editing again
            course.setApprovedByManager(null); // Clear approval
            course.setApprovedAt(null);
            log.info("Course rejected by user ID: {} with reason: {}", currentUser.getId(), request.getRejectionReason());
        }

        course.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        Course updatedCourse = courseRepository.save(course);
        log.info("Course approval processed: {}", updatedCourse.getCode());

        return convertToDTO(updatedCourse);
    }

    /**
     * Delete a course (soft delete)
     */
    @Override
    public void deleteCourse(Long id) {
        log.info("Deleting course ID: {}", id);

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Course not found with ID: {}", id);
                    return new CustomException(ErrorCode.COURSE_NOT_FOUND);
                });

        // Check if course is being used by any classes
        long classCount = classRepository.countByCourseId(id);
        if (classCount > 0) {
            log.error("Cannot delete course in use by {} classes", classCount);
            throw new CustomException(ErrorCode.COURSE_IN_USE);
        }

        // Soft delete
        course.setStatus("inactive");
        course.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        courseRepository.save(course);

        log.info("Course soft deleted: {}", course.getCode());
    }

    // ==================== Helper Methods ====================

    /**
     * Convert Course entity to CourseDTO
     */
    private CourseDTO convertToDTO(Course course) {
        CourseDTO dto = new CourseDTO();
        dto.setId(course.getId());
        dto.setSubjectId(course.getSubject() != null ? course.getSubject().getId() : null);
        dto.setLevelId(course.getLevel() != null ? course.getLevel().getId() : null);
        dto.setCode(course.getCode());
        dto.setName(course.getName());
        dto.setVersion(course.getVersion());
        dto.setDescription(course.getDescription());
        dto.setTotalHours(course.getTotalHours() != null ? course.getTotalHours().doubleValue() : null);
        dto.setDurationWeeks(course.getDurationWeeks());
        dto.setSessionPerWeek(course.getSessionPerWeek());
        dto.setHoursPerSession(course.getHoursPerSession() != null ? course.getHoursPerSession().doubleValue() : null);
        dto.setStatus(course.getStatus());
        dto.setApprovedByManager(course.getApprovedByManager() != null ? course.getApprovedByManager().getId() : null);
        dto.setApprovedAt(course.getApprovedAt() != null ? course.getApprovedAt().toLocalDateTime() : null);
        dto.setCreatedBy(course.getCreatedBy() != null ? course.getCreatedBy().getId() : null);
        dto.setCreatedAt(course.getCreatedAt() != null ? course.getCreatedAt().toLocalDateTime() : null);

        // Count phases and sessions
        dto.setPhasesCount((int) coursePhaseRepository.countByCourseId(course.getId()));

        return dto;
    }

    /**
     * Convert Course entity to CourseDetailDTO
     */
    private CourseDetailDTO convertToDetailDTO(Course course) {
        CourseDetailDTO dto = new CourseDetailDTO();
        dto.setId(course.getId());
        dto.setSubjectId(course.getSubject() != null ? course.getSubject().getId() : null);
        dto.setLevelId(course.getLevel() != null ? course.getLevel().getId() : null);
        dto.setCode(course.getCode());
        dto.setName(course.getName());
        dto.setVersion(course.getVersion());
        dto.setDescription(course.getDescription());
        dto.setTotalHours(course.getTotalHours() != null ? course.getTotalHours().doubleValue() : null);
        dto.setDurationWeeks(course.getDurationWeeks());
        dto.setSessionPerWeek(course.getSessionPerWeek());
        dto.setHoursPerSession(course.getHoursPerSession() != null ? course.getHoursPerSession().doubleValue() : null);
        dto.setPrerequisites(course.getPrerequisites());
        dto.setTargetAudience(course.getTargetAudience());
        dto.setTeachingMethods(course.getTeachingMethods());
        dto.setStatus(course.getStatus());
        dto.setApprovedByManager(course.getApprovedByManager() != null ? course.getApprovedByManager().getId() : null);
        dto.setApprovedAt(course.getApprovedAt() != null ? course.getApprovedAt().toLocalDateTime() : null);
        dto.setCreatedBy(course.getCreatedBy() != null ? course.getCreatedBy().getId() : null);
        dto.setCreatedAt(course.getCreatedAt() != null ? course.getCreatedAt().toLocalDateTime() : null);

        // Load phases if needed
        List<CoursePhase> phases = coursePhaseRepository.findByCourseIdOrderBySortOrderAsc(course.getId());
        dto.setPhases(phases.stream().map(this::convertPhaseToDTO).collect(Collectors.toList()));

        return dto;
    }

    /**
     * Convert CoursePhase to CoursePhaseDTO
     */
    private CoursePhaseDTO convertPhaseToDTO(CoursePhase phase) {
        CoursePhaseDTO dto = new CoursePhaseDTO();
        dto.setId(phase.getId());
        dto.setCourseId(phase.getCourse() != null ? phase.getCourse().getId() : null);
        dto.setPhaseNumber(phase.getPhaseNumber());
        dto.setName(phase.getName());
        dto.setDurationWeeks(phase.getDurationWeeks());
        dto.setLearningFocus(phase.getLearningFocus());
        dto.setSortOrder(phase.getSortOrder());
        dto.setCreatedAt(phase.getCreatedAt() != null ? phase.getCreatedAt().toLocalDateTime() : null);
        return dto;
    }

    /**
     * Calculate MD5 hash checksum for course content
     */
    private String calculateHashChecksum(Course course) {
        try {
            StringBuilder content = new StringBuilder();
            content.append(course.getCode() != null ? course.getCode() : "");
            content.append(course.getName() != null ? course.getName() : "");
            content.append(course.getTotalHours() != null ? course.getTotalHours() : "");
            content.append(course.getDurationWeeks() != null ? course.getDurationWeeks() : "");
            content.append(course.getSessionPerWeek() != null ? course.getSessionPerWeek() : "");
            content.append(course.getHoursPerSession() != null ? course.getHoursPerSession() : "");
            content.append(course.getPrerequisites() != null ? course.getPrerequisites() : "");
            content.append(course.getTargetAudience() != null ? course.getTargetAudience() : "");
            content.append(course.getTeachingMethods() != null ? course.getTeachingMethods() : "");

            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(content.toString().getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("MD5 algorithm not found", e);
            return null;
        }
    }

    /**
     * Get current authenticated user
     */
    private UserAccount getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            return userAccountRepository.findById(userDetails.getUserId())
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        }
        throw new CustomException(ErrorCode.UNAUTHORIZED);
    }
}
