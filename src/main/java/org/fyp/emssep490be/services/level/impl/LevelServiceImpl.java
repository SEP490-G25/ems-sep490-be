package org.fyp.emssep490be.services.level.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fyp.emssep490be.dtos.level.CreateLevelRequestDTO;
import org.fyp.emssep490be.dtos.level.LevelDTO;
import org.fyp.emssep490be.dtos.level.UpdateLevelRequestDTO;
import org.fyp.emssep490be.entities.Level;
import org.fyp.emssep490be.entities.Subject;
import org.fyp.emssep490be.exceptions.CustomException;
import org.fyp.emssep490be.exceptions.ErrorCode;
import org.fyp.emssep490be.repositories.CourseRepository;
import org.fyp.emssep490be.repositories.LevelRepository;
import org.fyp.emssep490be.repositories.SubjectRepository;
import org.fyp.emssep490be.services.level.LevelService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of LevelService for Level management operations
 * Levels are nested under Subjects
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LevelServiceImpl implements LevelService {

    private final LevelRepository levelRepository;
    private final SubjectRepository subjectRepository;
    private final CourseRepository courseRepository;

    /**
     * Get all levels for a specific subject, ordered by sort order
     *
     * @param subjectId Subject ID
     * @return List of levels ordered by sortOrder
     */
    @Override
    @Transactional(readOnly = true)
    public List<LevelDTO> getLevelsBySubject(Long subjectId) {
        log.info("Getting levels for subject ID: {}", subjectId);

        // Validate subject exists
        if (!subjectRepository.existsById(subjectId)) {
            log.error("Subject not found: {}", subjectId);
            throw new CustomException(ErrorCode.LEVEL_INVALID_SUBJECT);
        }

        List<Level> levels = levelRepository.findBySubjectIdOrderBySortOrderAsc(subjectId);

        log.info("Retrieved {} levels for subject ID: {}", levels.size(), subjectId);
        return levels.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get level by ID, ensuring it belongs to the specified subject
     *
     * @param subjectId Subject ID
     * @param id Level ID
     * @return Level information
     */
    @Override
    @Transactional(readOnly = true)
    public LevelDTO getLevelById(Long subjectId, Long id) {
        log.info("Getting level ID: {} for subject ID: {}", id, subjectId);

        Level level = levelRepository.findByIdAndSubjectId(id, subjectId)
                .orElseThrow(() -> {
                    log.error("Level not found or doesn't belong to subject. Level ID: {}, Subject ID: {}", id, subjectId);
                    return new CustomException(ErrorCode.LEVEL_NOT_FOUND);
                });

        return convertToDTO(level);
    }

    /**
     * Create a new level under a subject
     *
     * @param subjectId Subject ID
     * @param request Level creation data
     * @return Created level information
     */
    @Override
    public LevelDTO createLevel(Long subjectId, CreateLevelRequestDTO request) {
        log.info("Creating level '{}' for subject ID: {}", request.getCode(), subjectId);

        // Validate subject exists
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> {
                    log.error("Subject not found: {}", subjectId);
                    return new CustomException(ErrorCode.LEVEL_INVALID_SUBJECT);
                });

        // Validate unique code within subject
        if (levelRepository.existsByCodeAndSubjectId(request.getCode(), subjectId)) {
            log.error("Level code '{}' already exists for subject ID: {}", request.getCode(), subjectId);
            throw new CustomException(ErrorCode.LEVEL_CODE_DUPLICATE);
        }

        // Validate unique sort order within subject
        if (levelRepository.existsBySortOrderAndSubjectId(request.getSortOrder(), subjectId)) {
            log.error("Sort order {} already exists for subject ID: {}", request.getSortOrder(), subjectId);
            throw new CustomException(ErrorCode.LEVEL_SORT_ORDER_DUPLICATE);
        }

        // Create level entity
        Level level = new Level();
        level.setSubject(subject);
        level.setCode(request.getCode());
        level.setName(request.getName());
        level.setStandardType(request.getStandardType());
        level.setExpectedDurationHours(request.getExpectedDurationHours());
        level.setSortOrder(request.getSortOrder());
        level.setDescription(request.getDescription());
        level.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        level.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        Level savedLevel = levelRepository.save(level);

        log.info("Level created successfully with ID: {}", savedLevel.getId());
        return convertToDTO(savedLevel);
    }

    /**
     * Update an existing level
     *
     * @param subjectId Subject ID
     * @param id Level ID
     * @param request Update data
     * @return Updated level information
     */
    @Override
    public LevelDTO updateLevel(Long subjectId, Long id, UpdateLevelRequestDTO request) {
        log.info("Updating level ID: {} for subject ID: {}", id, subjectId);

        // Find level and validate it belongs to subject
        Level level = levelRepository.findByIdAndSubjectId(id, subjectId)
                .orElseThrow(() -> {
                    log.error("Level not found or doesn't belong to subject. Level ID: {}, Subject ID: {}", id, subjectId);
                    return new CustomException(ErrorCode.LEVEL_NOT_FOUND);
                });

        // Update fields if provided
        if (request.getName() != null && !request.getName().isBlank()) {
            level.setName(request.getName());
        }

        if (request.getStandardType() != null) {
            level.setStandardType(request.getStandardType());
        }

        if (request.getExpectedDurationHours() != null) {
            level.setExpectedDurationHours(request.getExpectedDurationHours());
        }

        if (request.getSortOrder() != null) {
            // Validate sort order uniqueness if changed
            if (!request.getSortOrder().equals(level.getSortOrder())) {
                if (levelRepository.existsBySortOrderAndSubjectId(request.getSortOrder(), subjectId)) {
                    log.error("Sort order {} already exists for subject ID: {}", request.getSortOrder(), subjectId);
                    throw new CustomException(ErrorCode.LEVEL_SORT_ORDER_DUPLICATE);
                }
                level.setSortOrder(request.getSortOrder());
            }
        }

        if (request.getDescription() != null) {
            level.setDescription(request.getDescription());
        }

        level.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        Level updatedLevel = levelRepository.save(level);

        log.info("Level updated successfully: {}", id);
        return convertToDTO(updatedLevel);
    }

    /**
     * Delete a level (hard delete)
     * Can only delete if level has no courses
     *
     * @param subjectId Subject ID
     * @param id Level ID
     */
    @Override
    public void deleteLevel(Long subjectId, Long id) {
        log.info("Deleting level ID: {} for subject ID: {}", id, subjectId);

        // Find level and validate it belongs to subject
        Level level = levelRepository.findByIdAndSubjectId(id, subjectId)
                .orElseThrow(() -> {
                    log.error("Level not found or doesn't belong to subject. Level ID: {}, Subject ID: {}", id, subjectId);
                    return new CustomException(ErrorCode.LEVEL_NOT_FOUND);
                });

        // Check if level has courses
        long coursesCount = courseRepository.countByLevelId(id);
        if (coursesCount > 0) {
            log.error("Cannot delete level with existing courses. Level ID: {}, Courses count: {}", id, coursesCount);
            throw new CustomException(ErrorCode.LEVEL_HAS_COURSES);
        }

        // Hard delete
        levelRepository.delete(level);

        log.info("Level deleted successfully: {}", id);
    }

    /**
     * Convert Level entity to LevelDTO
     */
    private LevelDTO convertToDTO(Level level) {
        return LevelDTO.builder()
                .id(level.getId())
                .subjectId(level.getSubject() != null ? level.getSubject().getId() : null)
                .code(level.getCode())
                .name(level.getName())
                .standardType(level.getStandardType())
                .expectedDurationHours(level.getExpectedDurationHours())
                .sortOrder(level.getSortOrder())
                .description(level.getDescription())
                .createdAt(level.getCreatedAt() != null ? level.getCreatedAt().toLocalDateTime() : null)
                .build();
    }
}
