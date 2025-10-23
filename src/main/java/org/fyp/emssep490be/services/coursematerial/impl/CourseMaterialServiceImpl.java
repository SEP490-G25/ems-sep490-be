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

        // Validate material has at least one context (course, phase, or courseSession)
        Long phaseId = request.getPhaseId();
        Long sessionId = request.getCourseSessionId();

        // Material can be at course level (phaseId and sessionId both null)
        // Or at phase level (phaseId not null, sessionId null)
        // Or at session level (both not null recommended, but sessionId alone acceptable)

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

        // Validate courseSession if provided
        CourseSession courseSession = null;
        if (sessionId != null) {
            courseSession = courseSessionRepository.findById(sessionId)
                    .orElseThrow(() -> {
                        log.error("CourseSession not found with ID: {}", sessionId);
                        return new CustomException(ErrorCode.COURSE_SESSION_NOT_FOUND);
                    });

            // Validate courseSession belongs to course
            if (!courseSession.getPhase().getCourse().getId().equals(courseId)) {
                log.error("CourseSession ID: {} does not belong to course ID: {}", sessionId, courseId);
                throw new CustomException(ErrorCode.INVALID_INPUT);
            }
        }

        // Validate title not empty
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            log.error("Material title cannot be empty");
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }

        // Validate file provided
        if (request.getFile() == null || request.getFile().isEmpty()) {
            log.error("Material file is required");
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }

        // TODO: Handle actual file upload to storage (S3 or local)
        // For MVP, we'll just store the original filename as URL
        String fileUrl = "/uploads/" + request.getFile().getOriginalFilename();

        // Get current user from security context
        // TODO: Get actual UserAccount entity from database
        // For now, we'll leave uploadedBy as null

        // Create material entity
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        CourseMaterial material = new CourseMaterial();
        material.setCourse(course);
        material.setPhase(phase);
        material.setCourseSession(courseSession);
        material.setTitle(request.getTitle());
        material.setUrl(fileUrl);
        // material.setUploadedBy(uploadedByUser); // TODO: Set when user management is ready
        material.setCreatedAt(now);
        material.setUpdatedAt(now);

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
        }

        if (material.getCourseSession() != null) {
            dto.setCourseSessionId(material.getCourseSession().getId());
        }

        dto.setTitle(material.getTitle());
        dto.setUrl(material.getUrl());

        if (material.getUploadedBy() != null) {
            dto.setUploadedBy(material.getUploadedBy().getId());
        }

        if (material.getCreatedAt() != null) {
            dto.setCreatedAt(material.getCreatedAt().toLocalDateTime());
        }

        return dto;
    }
}
