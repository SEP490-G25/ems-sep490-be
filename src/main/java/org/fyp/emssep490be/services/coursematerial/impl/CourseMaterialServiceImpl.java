package org.fyp.emssep490be.services.coursematerial.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fyp.emssep490be.dtos.coursematerial.CourseMaterialDTO;
import org.fyp.emssep490be.dtos.coursematerial.UploadMaterialRequestDTO;
import org.fyp.emssep490be.entities.Course;
import org.fyp.emssep490be.entities.CourseMaterial;
import org.fyp.emssep490be.entities.CoursePhase;
import org.fyp.emssep490be.entities.CourseSession;
import org.fyp.emssep490be.exceptions.CustomException;
import org.fyp.emssep490be.exceptions.ErrorCode;
import org.fyp.emssep490be.repositories.CourseMaterialRepository;
import org.fyp.emssep490be.repositories.CoursePhaseRepository;
import org.fyp.emssep490be.repositories.CourseRepository;
import org.fyp.emssep490be.repositories.CourseSessionRepository;
import org.fyp.emssep490be.services.coursematerial.CourseMaterialService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Implementation of CourseMaterialService for course material management
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CourseMaterialServiceImpl implements CourseMaterialService {

    private final CourseMaterialRepository courseMaterialRepository;
    private final CourseRepository courseRepository;
    private final CoursePhaseRepository coursePhaseRepository;
    private final CourseSessionRepository courseSessionRepository;

    /**
     * Upload course material
     * Material can be associated with course, phase, or session
     *
     * @param courseId Course ID
     * @param request Upload material request
     * @return Created material DTO
     */
    @Override
    public CourseMaterialDTO uploadMaterial(Long courseId, UploadMaterialRequestDTO request) {
        log.info("Uploading material for course ID: {} with title: {}", courseId, request.getTitle());

        // Validate course exists
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> {
                    log.error("Course not found with ID: {}", courseId);
                    return new CustomException(ErrorCode.COURSE_NOT_FOUND);
                });

        // Validate material has at least one context (course, phase, or session)
        Long phaseId = request.getPhaseId();
        Long sessionId = request.getSessionId();

        if (phaseId == null && sessionId == null) {
            log.error("Material must be associated with course, phase, or session");
            throw new CustomException(ErrorCode.MATERIAL_MUST_HAVE_CONTEXT);
        }

        // Validate phase if provided
        CoursePhase phase = null;
        if (phaseId != null) {
            phase = coursePhaseRepository.findById(phaseId)
                    .orElseThrow(() -> {
                        log.error("CoursePhase not found with ID: {}", phaseId);
                        return new CustomException(ErrorCode.PHASE_NOT_FOUND);
                    });

            // Validate phase belongs to course
            if (!phase.getCourse().getId().equals(courseId)) {
                log.error("Phase ID: {} does not belong to course ID: {}", phaseId, courseId);
                throw new CustomException(ErrorCode.INVALID_INPUT);
            }
        }

        // Validate session if provided
        CourseSession session = null;
        if (sessionId != null) {
            session = courseSessionRepository.findById(sessionId)
                    .orElseThrow(() -> {
                        log.error("CourseSession not found with ID: {}", sessionId);
                        return new CustomException(ErrorCode.COURSE_SESSION_NOT_FOUND);
                    });

            // Validate session belongs to course
            if (!session.getPhase().getCourse().getId().equals(courseId)) {
                log.error("Session ID: {} does not belong to course ID: {}", sessionId, courseId);
                throw new CustomException(ErrorCode.INVALID_INPUT);
            }
        }

        // Validate title not empty
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            log.error("Material title cannot be empty");
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }

        // Validate file URL not empty
        if (request.getFileUrl() == null || request.getFileUrl().trim().isEmpty()) {
            log.error("Material file URL cannot be empty");
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }

        // Get current user from security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String uploadedBy = authentication != null ? authentication.getName() : "system";

        // Create material entity
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        CourseMaterial material = new CourseMaterial();
        material.setCourse(course);
        material.setPhase(phase);
        material.setSession(session);
        material.setTitle(request.getTitle());
        material.setDescription(request.getDescription());
        material.setFileUrl(request.getFileUrl());
        material.setFileType(request.getFileType());
        material.setUploadedBy(uploadedBy);
        material.setUploadedAt(now);

        // Save and return
        CourseMaterial savedMaterial = courseMaterialRepository.save(material);
        log.info("Material uploaded successfully with ID: {}", savedMaterial.getId());

        return convertToDTO(savedMaterial);
    }

    /**
     * Delete course material
     *
     * @param courseId Course ID
     * @param id Material ID
     */
    @Override
    public void deleteMaterial(Long courseId, Long id) {
        log.info("Deleting material ID: {} for course ID: {}", id, courseId);

        // Validate material exists and belongs to course
        CourseMaterial material = courseMaterialRepository.findByIdAndCourseId(id, courseId)
                .orElseThrow(() -> {
                    log.error("Material not found with ID: {} for course ID: {}", id, courseId);
                    return new CustomException(ErrorCode.COURSE_MATERIAL_NOT_FOUND);
                });

        // Delete material record
        // Note: In production, this should also delete the physical file from storage
        courseMaterialRepository.delete(material);
        log.info("Material deleted successfully with ID: {}", id);
    }

    /**
     * Convert CourseMaterial entity to DTO
     *
     * @param material CourseMaterial entity
     * @return CourseMaterial DTO
     */
    private CourseMaterialDTO convertToDTO(CourseMaterial material) {
        CourseMaterialDTO dto = new CourseMaterialDTO();
        dto.setId(material.getId());
        dto.setCourseId(material.getCourse().getId());

        if (material.getPhase() != null) {
            dto.setPhaseId(material.getPhase().getId());
            dto.setPhaseNumber(material.getPhase().getPhaseNumber());
        }

        if (material.getSession() != null) {
            dto.setSessionId(material.getSession().getId());
            dto.setSessionSequence(material.getSession().getSequenceNo());
        }

        dto.setTitle(material.getTitle());
        dto.setDescription(material.getDescription());
        dto.setFileUrl(material.getFileUrl());
        dto.setFileType(material.getFileType());
        dto.setUploadedBy(material.getUploadedBy());
        dto.setUploadedAt(material.getUploadedAt());

        return dto;
    }
}
