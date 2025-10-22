package org.fyp.emssep490be.services.coursephase.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fyp.emssep490be.dtos.coursephase.CoursePhaseDTO;
import org.fyp.emssep490be.dtos.coursephase.CreateCoursePhaseRequestDTO;
import org.fyp.emssep490be.dtos.coursephase.UpdateCoursePhaseRequestDTO;
import org.fyp.emssep490be.entities.Course;
import org.fyp.emssep490be.entities.CoursePhase;
import org.fyp.emssep490be.exceptions.CustomException;
import org.fyp.emssep490be.exceptions.ErrorCode;
import org.fyp.emssep490be.repositories.CoursePhaseRepository;
import org.fyp.emssep490be.repositories.CourseRepository;
import org.fyp.emssep490be.repositories.CourseSessionRepository;
import org.fyp.emssep490be.services.coursephase.CoursePhaseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of CoursePhaseService
 * Manages course phases (CRUD operations)
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CoursePhaseServiceImpl implements CoursePhaseService {

    private final CoursePhaseRepository coursePhaseRepository;
    private final CourseRepository courseRepository;
    private final CourseSessionRepository courseSessionRepository;

    /**
     * Get all phases for a course
     * @param courseId Course ID
     * @return List of course phases ordered by sort_order
     */
    @Override
    @Transactional(readOnly = true)
    public List<CoursePhaseDTO> getPhasesByCourse(Long courseId) {
        log.info("Getting phases for course ID: {}", courseId);

        // Validate course exists
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> {
                    log.error("Course not found with ID: {}", courseId);
                    return new CustomException(ErrorCode.COURSE_NOT_FOUND);
                });

        // Get all phases ordered by sort_order
        List<CoursePhase> phases = coursePhaseRepository.findByCourseIdOrderBySortOrderAsc(courseId);

        log.info("Retrieved {} phases for course: {}", phases.size(), course.getCode());

        // Map to DTOs with sessions count
        return phases.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Create a new phase for a course
     * @param courseId Course ID
     * @param request Phase creation request
     * @return Created phase DTO
     */
    @Override
    public CoursePhaseDTO createPhase(Long courseId, CreateCoursePhaseRequestDTO request) {
        log.info("Creating phase for course ID: {}, phase number: {}", courseId, request.getPhaseNumber());

        // Validate course exists
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> {
                    log.error("Course not found with ID: {}", courseId);
                    return new CustomException(ErrorCode.COURSE_NOT_FOUND);
                });

        // Validate course is in draft status
        if (!"draft".equals(course.getStatus())) {
            log.error("Cannot modify course - status: {}", course.getStatus());
            throw new CustomException(ErrorCode.COURSE_CANNOT_BE_MODIFIED);
        }

        // Check unique constraint: (course_id, phase_number)
        if (coursePhaseRepository.existsByPhaseNumberAndCourseId(request.getPhaseNumber(), courseId)) {
            log.error("Phase number {} already exists for course ID: {}", request.getPhaseNumber(), courseId);
            throw new CustomException(ErrorCode.PHASE_NUMBER_DUPLICATE);
        }

        // Build CoursePhase entity
        CoursePhase phase = new CoursePhase();
        phase.setCourse(course);
        phase.setPhaseNumber(request.getPhaseNumber());
        phase.setName(request.getName());
        phase.setDurationWeeks(request.getDurationWeeks());
        phase.setLearningFocus(request.getLearningFocus());
        phase.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : request.getPhaseNumber());

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        phase.setCreatedAt(now);
        phase.setUpdatedAt(now);

        // Save phase
        CoursePhase savedPhase = coursePhaseRepository.save(phase);
        log.info("Phase created successfully with ID: {}", savedPhase.getId());

        return convertToDTO(savedPhase);
    }

    /**
     * Update an existing phase
     * @param phaseId Phase ID
     * @param request Phase update request
     * @return Updated phase DTO
     */
    @Override
    public CoursePhaseDTO updatePhase(Long phaseId, UpdateCoursePhaseRequestDTO request) {
        log.info("Updating phase ID: {}", phaseId);

        // Validate phase exists
        CoursePhase phase = coursePhaseRepository.findById(phaseId)
                .orElseThrow(() -> {
                    log.error("Phase not found with ID: {}", phaseId);
                    return new CustomException(ErrorCode.PHASE_NOT_FOUND);
                });

        // Validate course is in draft status
        if (!"draft".equals(phase.getCourse().getStatus())) {
            log.error("Cannot modify phase - course status: {}", phase.getCourse().getStatus());
            throw new CustomException(ErrorCode.COURSE_CANNOT_BE_MODIFIED);
        }

        // Update fields
        phase.setName(request.getName());
        phase.setDurationWeeks(request.getDurationWeeks());
        phase.setLearningFocus(request.getLearningFocus());

        if (request.getSortOrder() != null) {
            phase.setSortOrder(request.getSortOrder());
        }

        phase.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        // Save phase
        CoursePhase updatedPhase = coursePhaseRepository.save(phase);
        log.info("Phase updated successfully: phase number {}", updatedPhase.getPhaseNumber());

        return convertToDTO(updatedPhase);
    }

    /**
     * Delete a phase
     * @param phaseId Phase ID
     */
    @Override
    public void deletePhase(Long phaseId) {
        log.info("Deleting phase ID: {}", phaseId);

        // Validate phase exists
        CoursePhase phase = coursePhaseRepository.findById(phaseId)
                .orElseThrow(() -> {
                    log.error("Phase not found with ID: {}", phaseId);
                    return new CustomException(ErrorCode.PHASE_NOT_FOUND);
                });

        // Validate course is in draft status
        if (!"draft".equals(phase.getCourse().getStatus())) {
            log.error("Cannot modify phase - course status: {}", phase.getCourse().getStatus());
            throw new CustomException(ErrorCode.COURSE_CANNOT_BE_MODIFIED);
        }

        // Check if phase has course sessions
        long sessionsCount = courseSessionRepository.countByPhaseId(phaseId);
        if (sessionsCount > 0) {
            log.error("Cannot delete phase with {} course sessions", sessionsCount);
            throw new CustomException(ErrorCode.PHASE_HAS_SESSIONS);
        }

        // Delete phase
        coursePhaseRepository.delete(phase);
        log.info("Phase deleted successfully: phase number {}", phase.getPhaseNumber());
    }

    /**
     * Convert CoursePhase entity to DTO
     * @param phase CoursePhase entity
     * @return CoursePhaseDTO
     */
    private CoursePhaseDTO convertToDTO(CoursePhase phase) {
        CoursePhaseDTO dto = new CoursePhaseDTO();
        dto.setId(phase.getId());
        dto.setCourseId(phase.getCourse().getId());
        dto.setPhaseNumber(phase.getPhaseNumber());
        dto.setName(phase.getName());
        dto.setDurationWeeks(phase.getDurationWeeks());
        dto.setLearningFocus(phase.getLearningFocus());
        dto.setSortOrder(phase.getSortOrder());

        // Count course sessions
        long sessionsCount = courseSessionRepository.countByPhaseId(phase.getId());
        dto.setSessionsCount((int) sessionsCount);

        // Convert OffsetDateTime to LocalDateTime for DTO
        if (phase.getCreatedAt() != null) {
            dto.setCreatedAt(phase.getCreatedAt().toLocalDateTime());
        }

        return dto;
    }
}
