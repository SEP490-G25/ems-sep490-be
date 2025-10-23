package org.fyp.emssep490be.services.plo.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fyp.emssep490be.dtos.plo.CreatePloRequestDTO;
import org.fyp.emssep490be.dtos.plo.PloDTO;
import org.fyp.emssep490be.entities.Plo;
import org.fyp.emssep490be.entities.Subject;
import org.fyp.emssep490be.exceptions.CustomException;
import org.fyp.emssep490be.exceptions.ErrorCode;
import org.fyp.emssep490be.repositories.PloCloMappingRepository;
import org.fyp.emssep490be.repositories.PloRepository;
import org.fyp.emssep490be.repositories.SubjectRepository;
import org.fyp.emssep490be.services.plo.PloService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of PloService for PLO (Program Learning Outcomes) management
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PloServiceImpl implements PloService {

    private final PloRepository ploRepository;
    private final SubjectRepository subjectRepository;
    private final PloCloMappingRepository ploCloMappingRepository;

    /**
     * Get all PLOs for a subject
     *
     * @param subjectId Subject ID
     * @return List of PLOs with mapping counts
     */
    @Override
    @Transactional(readOnly = true)
    public List<PloDTO> getPlosBySubject(Long subjectId) {
        log.info("Getting PLOs for subject ID: {}", subjectId);

        // Validate subject exists
        if (!subjectRepository.existsById(subjectId)) {
            log.error("Subject not found with ID: {}", subjectId);
            throw new CustomException(ErrorCode.SUBJECT_NOT_FOUND);
        }

        // Get PLOs for subject
        List<Plo> plos = ploRepository.findBySubjectId(subjectId);

        if (plos.isEmpty()) {
            log.warn("No PLOs found for subject ID: {}", subjectId);
        }

        // Convert to DTOs with mapping counts
        return plos.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Create a new PLO
     *
     * @param subjectId Subject ID
     * @param request PLO creation request
     * @return Created PLO DTO
     */
    @Override
    public PloDTO createPlo(Long subjectId, CreatePloRequestDTO request) {
        log.info("Creating PLO for subject ID: {} with code: {}", subjectId, request.getCode());

        // Validate subject exists
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> {
                    log.error("Subject not found with ID: {}", subjectId);
                    return new CustomException(ErrorCode.SUBJECT_NOT_FOUND);
                });

        // Validate code unique for subject
        if (ploRepository.existsByCodeAndSubjectId(request.getCode(), subjectId)) {
            log.error("PLO code '{}' already exists for subject ID: {}", request.getCode(), subjectId);
            throw new CustomException(ErrorCode.PLO_CODE_DUPLICATE);
        }

        // Validate code format (uppercase, numbers, dashes only)
        if (!request.getCode().matches("^[A-Z0-9-]+$")) {
            log.error("Invalid PLO code format: {}", request.getCode());
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }

        // Validate description not empty
        if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
            log.error("PLO description cannot be empty");
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }

        // Create PLO entity
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        Plo plo = new Plo();
        plo.setSubject(subject);
        plo.setCode(request.getCode());
        plo.setDescription(request.getDescription());
        plo.setCreatedAt(now);
        plo.setUpdatedAt(now);

        // Save and return
        Plo savedPlo = ploRepository.save(plo);
        log.info("PLO created successfully with ID: {}", savedPlo.getId());

        return convertToDTO(savedPlo);
    }

    /**
     * Delete a PLO
     *
     * @param subjectId Subject ID
     * @param id PLO ID
     */
    @Override
    public void deletePlo(Long subjectId, Long id) {
        log.info("Deleting PLO ID: {} for subject ID: {}", id, subjectId);

        // Validate PLO exists and belongs to subject
        Plo plo = ploRepository.findByIdAndSubjectId(id, subjectId)
                .orElseThrow(() -> {
                    log.error("PLO not found with ID: {} for subject ID: {}", id, subjectId);
                    return new CustomException(ErrorCode.PLO_NOT_FOUND);
                });

        // Check if PLO has CLO mappings
        if (ploCloMappingRepository.existsByPloId(id)) {
            log.error("Cannot delete PLO ID: {} - has existing CLO mappings", id);
            throw new CustomException(ErrorCode.PLO_HAS_MAPPINGS);
        }

        // Delete PLO
        ploRepository.delete(plo);
        log.info("PLO deleted successfully with ID: {}", id);
    }

    /**
     * Convert PLO entity to DTO
     *
     * @param plo PLO entity
     * @return PLO DTO with mapping count
     */
    private PloDTO convertToDTO(Plo plo) {
        PloDTO dto = new PloDTO();
        dto.setId(plo.getId());
        dto.setSubjectId(plo.getSubject().getId());
        dto.setCode(plo.getCode());
        dto.setDescription(plo.getDescription());

        // Get mapped CLOs count
        long mappedClosCount = ploCloMappingRepository.countByPloId(plo.getId());
        dto.setMappedClosCount((int) mappedClosCount);

        return dto;
    }
}
