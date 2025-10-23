package org.fyp.emssep490be.services.clo.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fyp.emssep490be.dtos.clo.CloDTO;
import org.fyp.emssep490be.dtos.clo.CreateCloRequestDTO;
import org.fyp.emssep490be.dtos.clo.MappingRequestDTO;
import org.fyp.emssep490be.entities.*;
import org.fyp.emssep490be.entities.ids.CourseSessionCloMappingId;
import org.fyp.emssep490be.entities.ids.PloCloMappingId;
import org.fyp.emssep490be.exceptions.CustomException;
import org.fyp.emssep490be.exceptions.ErrorCode;
import org.fyp.emssep490be.repositories.*;
import org.fyp.emssep490be.services.clo.CloService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of CloService for CLO (Course Learning Outcomes) management
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CloServiceImpl implements CloService {

    private final CloRepository cloRepository;
    private final CourseRepository courseRepository;
    private final PloRepository ploRepository;
    private final CourseSessionRepository courseSessionRepository;
    private final PloCloMappingRepository ploCloMappingRepository;
    private final CourseSessionCloMappingRepository courseSessionCloMappingRepository;

    /**
     * Get all CLOs for a course with mapped PLOs
     *
     * @param courseId Course ID
     * @return List of CLOs with mapped PLOs
     */
    @Override
    @Transactional(readOnly = true)
    public List<CloDTO> getClosByCourse(Long courseId) {
        log.info("Getting CLOs for course ID: {}", courseId);

        // Validate course exists
        if (!courseRepository.existsById(courseId)) {
            log.error("Course not found with ID: {}", courseId);
            throw new CustomException(ErrorCode.COURSE_NOT_FOUND);
        }

        // Get CLOs for course
        List<Clo> clos = cloRepository.findByCourseId(courseId);

        if (clos.isEmpty()) {
            log.warn("No CLOs found for course ID: {}", courseId);
        }

        // Convert to DTOs with mapped PLOs
        return clos.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Create a new CLO for a course
     *
     * @param courseId Course ID
     * @param request CLO creation request
     * @return Created CLO DTO
     */
    @Override
    public CloDTO createClo(Long courseId, CreateCloRequestDTO request) {
        log.info("Creating CLO for course ID: {} with code: {}", courseId, request.getCode());

        // Validate course exists
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> {
                    log.error("Course not found with ID: {}", courseId);
                    return new CustomException(ErrorCode.COURSE_NOT_FOUND);
                });

        // Validate code unique for course
        if (cloRepository.existsByCodeAndCourseId(request.getCode(), courseId)) {
            log.error("CLO code '{}' already exists for course ID: {}", request.getCode(), courseId);
            throw new CustomException(ErrorCode.CLO_CODE_DUPLICATE);
        }

        // Validate code format (uppercase, numbers, dashes only)
        if (!request.getCode().matches("^[A-Z0-9-]+$")) {
            log.error("Invalid CLO code format: {}", request.getCode());
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }

        // Validate description not empty
        if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
            log.error("CLO description cannot be empty");
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }

        // Create CLO entity
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        Clo clo = new Clo();
        clo.setCourse(course);
        clo.setCode(request.getCode());
        clo.setDescription(request.getDescription());
        clo.setCreatedAt(now);
        clo.setUpdatedAt(now);

        // Save and return
        Clo savedClo = cloRepository.save(clo);
        log.info("CLO created successfully with ID: {}", savedClo.getId());

        return convertToDTO(savedClo);
    }

    /**
     * Map a PLO to a CLO
     * CRITICAL: Validates that PLO and CLO belong to the same subject
     *
     * @param ploId PLO ID
     * @param cloId CLO ID
     * @param request Mapping request
     * @return Mapping result with PLO and CLO details
     */
    @Override
    public Map<String, Object> mapPloToClo(Long ploId, Long cloId, MappingRequestDTO request) {
        log.info("Mapping PLO ID: {} to CLO ID: {}", ploId, cloId);

        // Validate PLO exists
        Plo plo = ploRepository.findById(ploId)
                .orElseThrow(() -> {
                    log.error("PLO not found with ID: {}", ploId);
                    return new CustomException(ErrorCode.PLO_NOT_FOUND);
                });

        // Validate CLO exists
        Clo clo = cloRepository.findById(cloId)
                .orElseThrow(() -> {
                    log.error("CLO not found with ID: {}", cloId);
                    return new CustomException(ErrorCode.CLO_NOT_FOUND);
                });

        // CRITICAL: Validate PLO and CLO belong to same subject
        Long ploSubjectId = plo.getSubject().getId();
        Long cloSubjectId = clo.getCourse().getSubject().getId();

        if (!ploSubjectId.equals(cloSubjectId)) {
            log.error("PLO (subject ID: {}) and CLO (subject ID: {}) must belong to the same subject",
                    ploSubjectId, cloSubjectId);
            throw new CustomException(ErrorCode.PLO_CLO_SUBJECT_MISMATCH);
        }

        // Check if mapping already exists
        if (ploCloMappingRepository.existsByPloIdAndCloId(ploId, cloId)) {
            log.error("PLO-CLO mapping already exists: PLO ID: {}, CLO ID: {}", ploId, cloId);
            throw new CustomException(ErrorCode.PLO_CLO_MAPPING_ALREADY_EXISTS);
        }

        // Create mapping
        PloCloMappingId mappingId = new PloCloMappingId(ploId, cloId);
        PloCloMapping mapping = new PloCloMapping();
        mapping.setId(mappingId);
        mapping.setPlo(plo);
        mapping.setClo(clo);

        if (request.getNote() != null && !request.getNote().trim().isEmpty()) {
            mapping.setNote(request.getNote());
        }

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        mapping.setCreatedAt(now);

        ploCloMappingRepository.save(mapping);
        log.info("PLO-CLO mapping created successfully: PLO ID: {}, CLO ID: {}", ploId, cloId);

        // Build response
        Map<String, Object> response = new HashMap<>();
        response.put("ploId", ploId);
        response.put("ploCode", plo.getCode());
        response.put("cloId", cloId);
        response.put("cloCode", clo.getCode());
        response.put("note", mapping.getNote());
        response.put("createdAt", mapping.getCreatedAt());

        return response;
    }

    /**
     * Map a CLO to a CourseSession
     * CRITICAL: Validates that CLO and CourseSession belong to the same course
     *
     * @param sessionId CourseSession ID
     * @param cloId CLO ID
     * @param request Mapping request
     * @return Mapping result with CLO and CourseSession details
     */
    @Override
    public Map<String, Object> mapCloToSession(Long sessionId, Long cloId, MappingRequestDTO request) {
        log.info("Mapping CLO ID: {} to CourseSession ID: {}", cloId, sessionId);

        // Validate CourseSession exists
        CourseSession courseSession = courseSessionRepository.findById(sessionId)
                .orElseThrow(() -> {
                    log.error("CourseSession not found with ID: {}", sessionId);
                    return new CustomException(ErrorCode.COURSE_SESSION_NOT_FOUND);
                });

        // Validate CLO exists
        Clo clo = cloRepository.findById(cloId)
                .orElseThrow(() -> {
                    log.error("CLO not found with ID: {}", cloId);
                    return new CustomException(ErrorCode.CLO_NOT_FOUND);
                });

        // CRITICAL: Validate CLO and CourseSession belong to same course
        Long sessionCourseId = courseSession.getPhase().getCourse().getId();
        Long cloCourseId = clo.getCourse().getId();

        if (!sessionCourseId.equals(cloCourseId)) {
            log.error("CLO (course ID: {}) and CourseSession (course ID: {}) must belong to the same course",
                    cloCourseId, sessionCourseId);
            throw new CustomException(ErrorCode.CLO_SESSION_COURSE_MISMATCH);
        }

        // Check if mapping already exists
        if (courseSessionCloMappingRepository.existsByCourseSessionIdAndCloId(sessionId, cloId)) {
            log.error("CLO-Session mapping already exists: Session ID: {}, CLO ID: {}", sessionId, cloId);
            throw new CustomException(ErrorCode.CLO_SESSION_MAPPING_ALREADY_EXISTS);
        }

        // Create mapping
        CourseSessionCloMappingId mappingId = new CourseSessionCloMappingId(sessionId, cloId);
        CourseSessionCloMapping mapping = new CourseSessionCloMapping();
        mapping.setId(mappingId);
        mapping.setCourseSession(courseSession);
        mapping.setClo(clo);

        if (request.getNote() != null && !request.getNote().trim().isEmpty()) {
            mapping.setNote(request.getNote());
        }

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        mapping.setCreatedAt(now);

        courseSessionCloMappingRepository.save(mapping);
        log.info("CLO-Session mapping created successfully: Session ID: {}, CLO ID: {}", sessionId, cloId);

        // Build response
        Map<String, Object> response = new HashMap<>();
        response.put("courseSessionId", sessionId);
        response.put("sessionSequence", courseSession.getSequenceNo());
        response.put("cloId", cloId);
        response.put("cloCode", clo.getCode());
        response.put("note", mapping.getNote());
        response.put("createdAt", mapping.getCreatedAt());

        return response;
    }

    /**
     * Delete a CLO
     * Validates that CLO belongs to course and has no existing mappings
     *
     * @param courseId Course ID
     * @param id CLO ID
     */
    @Override
    public void deleteClo(Long courseId, Long id) {
        log.info("Deleting CLO ID: {} for course ID: {}", id, courseId);

        // Validate CLO exists and belongs to course
        Clo clo = cloRepository.findByIdAndCourseId(id, courseId)
                .orElseThrow(() -> {
                    log.error("CLO not found with ID: {} for course ID: {}", id, courseId);
                    return new CustomException(ErrorCode.CLO_NOT_FOUND);
                });

        // Check if CLO has PLO mappings
        if (ploCloMappingRepository.existsByCloId(id)) {
            log.error("Cannot delete CLO ID: {} - has existing PLO mappings", id);
            throw new CustomException(ErrorCode.CLO_HAS_MAPPINGS);
        }

        // Check if CLO has session mappings
        if (courseSessionCloMappingRepository.existsByCloId(id)) {
            log.error("Cannot delete CLO ID: {} - has existing session mappings", id);
            throw new CustomException(ErrorCode.CLO_HAS_MAPPINGS);
        }

        // Delete CLO
        cloRepository.delete(clo);
        log.info("CLO deleted successfully with ID: {}", id);
    }

    /**
     * Convert CLO entity to DTO with mapped PLOs
     *
     * @param clo CLO entity
     * @return CLO DTO with mapped PLOs list
     */
    private CloDTO convertToDTO(Clo clo) {
        CloDTO dto = new CloDTO();
        dto.setId(clo.getId());
        dto.setCourseId(clo.getCourse().getId());
        dto.setCode(clo.getCode());
        dto.setDescription(clo.getDescription());

        // Get mapped PLOs
        List<PloCloMapping> mappings = ploCloMappingRepository.findByCloId(clo.getId());

        List<CloDTO.MappedPloDTO> mappedPlos = mappings.stream()
                .map(mapping -> {
                    CloDTO.MappedPloDTO ploDTO = new CloDTO.MappedPloDTO();
                    ploDTO.setPloId(mapping.getPlo().getId());
                    ploDTO.setPloCode(mapping.getPlo().getCode());
                    ploDTO.setDescription(mapping.getPlo().getDescription());
                    ploDTO.setNote(mapping.getNote());
                    return ploDTO;
                })
                .collect(Collectors.toList());

        dto.setMappedPlos(mappedPlos);

        return dto;
    }
}
