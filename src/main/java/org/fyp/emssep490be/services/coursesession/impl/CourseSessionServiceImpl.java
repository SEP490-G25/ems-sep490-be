package org.fyp.emssep490be.services.coursesession.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fyp.emssep490be.dtos.coursesession.CourseSessionDTO;
import org.fyp.emssep490be.dtos.coursesession.CreateCourseSessionRequestDTO;
import org.fyp.emssep490be.dtos.coursesession.UpdateCourseSessionRequestDTO;
import org.fyp.emssep490be.entities.CoursePhase;
import org.fyp.emssep490be.entities.CourseSession;
import org.fyp.emssep490be.entities.enums.Skill;
import org.fyp.emssep490be.exceptions.CustomException;
import org.fyp.emssep490be.exceptions.ErrorCode;
import org.fyp.emssep490be.repositories.CoursePhaseRepository;
import org.fyp.emssep490be.repositories.CourseSessionRepository;
import org.fyp.emssep490be.services.coursesession.CourseSessionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of CourseSessionService
 * Manages course sessions (CRUD operations)
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CourseSessionServiceImpl implements CourseSessionService {

    private final CourseSessionRepository courseSessionRepository;
    private final CoursePhaseRepository coursePhaseRepository;

    /**
     * Get all sessions for a phase
     * @param phaseId Phase ID
     * @return List of course sessions ordered by sequence_no
     */
    @Override
    @Transactional(readOnly = true)
    public List<CourseSessionDTO> getSessionsByPhase(Long phaseId) {
        log.info("Getting sessions for phase ID: {}", phaseId);

        // Validate phase exists
        CoursePhase phase = coursePhaseRepository.findById(phaseId)
                .orElseThrow(() -> {
                    log.error("Phase not found with ID: {}", phaseId);
                    return new CustomException(ErrorCode.PHASE_NOT_FOUND);
                });

        // Get all sessions ordered by sequence_no
        List<CourseSession> sessions = courseSessionRepository.findByPhaseIdOrderBySequenceNumberAsc(phaseId);

        log.info("Retrieved {} sessions for phase number: {}", sessions.size(), phase.getPhaseNumber());

        // Map to DTOs
        return sessions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Create a new session for a phase
     * @param phaseId Phase ID
     * @param request Session creation request
     * @return Created session DTO
     */
    @Override
    public CourseSessionDTO createSession(Long phaseId, CreateCourseSessionRequestDTO request) {
        log.info("Creating session for phase ID: {}, sequence number: {}", phaseId, request.getSequenceNo());

        // Validate phase exists
        CoursePhase phase = coursePhaseRepository.findById(phaseId)
                .orElseThrow(() -> {
                    log.error("Phase not found with ID: {}", phaseId);
                    return new CustomException(ErrorCode.PHASE_NOT_FOUND);
                });

        // Validate course is in draft status
        if (!"draft".equals(phase.getCourse().getStatus())) {
            log.error("Cannot modify course session - course status: {}", phase.getCourse().getStatus());
            throw new CustomException(ErrorCode.COURSE_CANNOT_BE_MODIFIED);
        }

        // Check unique constraint: (phase_id, sequence_no)
        if (courseSessionRepository.existsBySequenceNumberAndPhaseId(request.getSequenceNo(), phaseId)) {
            log.error("Sequence number {} already exists for phase ID: {}", request.getSequenceNo(), phaseId);
            throw new CustomException(ErrorCode.SESSION_SEQUENCE_DUPLICATE);
        }

        // Validate skill set
        List<Skill> skillSet = validateAndConvertSkillSet(request.getSkillSet());

        // Build CourseSession entity
        CourseSession session = new CourseSession();
        session.setPhase(phase);
        session.setSequenceNumber(request.getSequenceNo());
        session.setTopic(request.getTopic());
        session.setStudentTask(request.getStudentTask());
        session.setSkillSet(skillSet);

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        session.setCreatedAt(now);
        session.setUpdatedAt(now);

        // Save session
        CourseSession savedSession = courseSessionRepository.save(session);
        log.info("Session created successfully with ID: {}", savedSession.getId());

        return convertToDTO(savedSession);
    }

    /**
     * Update an existing session
     * @param sessionId Session ID
     * @param request Session update request
     * @return Updated session DTO
     */
    @Override
    public CourseSessionDTO updateSession(Long sessionId, UpdateCourseSessionRequestDTO request) {
        log.info("Updating session ID: {}", sessionId);

        // Validate session exists
        CourseSession session = courseSessionRepository.findById(sessionId)
                .orElseThrow(() -> {
                    log.error("Session not found with ID: {}", sessionId);
                    return new CustomException(ErrorCode.SESSION_NOT_FOUND);
                });

        // Validate course is in draft status
        if (!"draft".equals(session.getPhase().getCourse().getStatus())) {
            log.error("Cannot modify session - course status: {}", session.getPhase().getCourse().getStatus());
            throw new CustomException(ErrorCode.COURSE_CANNOT_BE_MODIFIED);
        }

        // Update fields
        if (request.getTopic() != null) {
            session.setTopic(request.getTopic());
        }
        if (request.getStudentTask() != null) {
            session.setStudentTask(request.getStudentTask());
        }
        if (request.getSkillSet() != null) {
            List<Skill> skillSet = validateAndConvertSkillSet(request.getSkillSet());
            session.setSkillSet(skillSet);
        }

        session.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        // Save session
        CourseSession updatedSession = courseSessionRepository.save(session);
        log.info("Session updated successfully: sequence number {}", updatedSession.getSequenceNumber());

        return convertToDTO(updatedSession);
    }

    /**
     * Delete a session
     * @param sessionId Session ID
     */
    @Override
    public void deleteSession(Long sessionId) {
        log.info("Deleting session ID: {}", sessionId);

        // Validate session exists
        CourseSession session = courseSessionRepository.findById(sessionId)
                .orElseThrow(() -> {
                    log.error("Session not found with ID: {}", sessionId);
                    return new CustomException(ErrorCode.SESSION_NOT_FOUND);
                });

        // Validate course is in draft status
        if (!"draft".equals(session.getPhase().getCourse().getStatus())) {
            log.error("Cannot modify session - course status: {}", session.getPhase().getCourse().getStatus());
            throw new CustomException(ErrorCode.COURSE_CANNOT_BE_MODIFIED);
        }

        // Check if session is being used in actual sessions
        long usageCount = courseSessionRepository.countSessionUsages(sessionId);
        if (usageCount > 0) {
            log.error("Cannot delete session used in {} actual sessions", usageCount);
            throw new CustomException(ErrorCode.SESSION_IN_USE);
        }

        // Delete session
        courseSessionRepository.delete(session);
        log.info("Session deleted successfully: sequence number {}", session.getSequenceNumber());
    }

    /**
     * Validate and convert skill set strings to Skill enum
     * @param skillSetStrings List of skill strings
     * @return List of Skill enums
     */
    private List<Skill> validateAndConvertSkillSet(List<String> skillSetStrings) {
        if (skillSetStrings == null || skillSetStrings.isEmpty()) {
            return new ArrayList<>();
        }

        List<Skill> skills = new ArrayList<>();
        for (String skillStr : skillSetStrings) {
            try {
                Skill skill = Skill.valueOf(skillStr.toUpperCase());
                skills.add(skill);
            } catch (IllegalArgumentException e) {
                log.error("Invalid skill value: {}", skillStr);
                throw new CustomException(ErrorCode.INVALID_SKILL_SET);
            }
        }
        return skills;
    }

    /**
     * Convert CourseSession entity to DTO
     * @param session CourseSession entity
     * @return CourseSessionDTO
     */
    private CourseSessionDTO convertToDTO(CourseSession session) {
        CourseSessionDTO dto = new CourseSessionDTO();
        dto.setId(session.getId());
        dto.setPhaseId(session.getPhase().getId());
        dto.setSequenceNo(session.getSequenceNumber());
        dto.setTopic(session.getTopic());
        dto.setStudentTask(session.getStudentTask());

        // Convert Skill enums to strings
        if (session.getSkillSet() != null) {
            List<String> skillStrings = session.getSkillSet().stream()
                    .map(Skill::name)
                    .collect(Collectors.toList());
            dto.setSkillSet(skillStrings);
        }

        // Convert OffsetDateTime to LocalDateTime for DTO
        if (session.getCreatedAt() != null) {
            dto.setCreatedAt(session.getCreatedAt().toLocalDateTime());
        }

        // Note: CLOs and materials are not loaded here for performance
        // They can be loaded separately if needed
        dto.setClos(new ArrayList<>());
        dto.setMaterials(new ArrayList<>());

        return dto;
    }
}
