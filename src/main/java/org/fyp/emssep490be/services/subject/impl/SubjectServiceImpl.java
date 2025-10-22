package org.fyp.emssep490be.services.subject.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fyp.emssep490be.dtos.common.PagedResponseDTO;
import org.fyp.emssep490be.dtos.common.PaginationDTO;
import org.fyp.emssep490be.dtos.level.LevelDTO;
import org.fyp.emssep490be.dtos.subject.CreateSubjectRequestDTO;
import org.fyp.emssep490be.dtos.subject.SubjectDTO;
import org.fyp.emssep490be.dtos.subject.SubjectDetailDTO;
import org.fyp.emssep490be.dtos.subject.UpdateSubjectRequestDTO;
import org.fyp.emssep490be.entities.Level;
import org.fyp.emssep490be.entities.Subject;
import org.fyp.emssep490be.entities.UserAccount;
import org.fyp.emssep490be.entities.enums.SubjectStatus;
import org.fyp.emssep490be.exceptions.CustomException;
import org.fyp.emssep490be.exceptions.ErrorCode;
import org.fyp.emssep490be.repositories.CourseRepository;
import org.fyp.emssep490be.repositories.LevelRepository;
import org.fyp.emssep490be.repositories.SubjectRepository;
import org.fyp.emssep490be.services.subject.SubjectService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of SubjectService for Subject management operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SubjectServiceImpl implements SubjectService {

    private final SubjectRepository subjectRepository;
    private final LevelRepository levelRepository;
    private final CourseRepository courseRepository;

    /**
     * Get all subjects with pagination and filtering by status
     *
     * @param status Filter by status (optional)
     * @param page Page number (1-based)
     * @param limit Items per page
     * @return Paginated list of subjects
     */
    @Override
    @Transactional(readOnly = true)
    public PagedResponseDTO<SubjectDTO> getAllSubjects(String status, Integer page, Integer limit) {
        log.info("Getting all subjects: status={}, page={}, limit={}", status, page, limit);

        // Set default pagination values
        int pageNumber = (page != null && page > 0) ? page - 1 : 0;
        int pageSize = (limit != null && limit > 0) ? limit : 10;

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("id").descending());

        Page<Subject> subjectPage;

        // Apply status filter if provided
        if (status != null && !status.isBlank()) {
            try {
                SubjectStatus subjectStatus = SubjectStatus.valueOf(status.toUpperCase());
                subjectPage = subjectRepository.findByStatus(subjectStatus, pageable);
            } catch (IllegalArgumentException e) {
                log.error("Invalid status value: {}", status);
                throw new CustomException(ErrorCode.INVALID_STATUS);
            }
        } else {
            subjectPage = subjectRepository.findAll(pageable);
        }

        List<SubjectDTO> subjectDTOs = subjectPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        PaginationDTO pagination = new PaginationDTO(
                subjectPage.getNumber() + 1,
                subjectPage.getTotalPages(),
                subjectPage.getTotalElements(),
                subjectPage.getSize()
        );

        log.info("Retrieved {} subjects", subjectDTOs.size());
        return new PagedResponseDTO<>(subjectDTOs, pagination);
    }

    /**
     * Get subject by ID with detailed information including levels
     *
     * @param id Subject ID
     * @return Subject details with levels
     */
    @Override
    @Transactional(readOnly = true)
    public SubjectDetailDTO getSubjectById(Long id) {
        log.info("Getting subject by ID: {}", id);

        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.SUBJECT_NOT_FOUND));

        // Fetch related levels
        List<Level> levels = levelRepository.findBySubjectIdOrderBySortOrderAsc(id);

        return convertToDetailDTO(subject, levels);
    }

    /**
     * Create a new subject
     *
     * @param request Subject creation data
     * @return Created subject information
     */
    @Override
    public SubjectDTO createSubject(CreateSubjectRequestDTO request) {
        log.info("Creating subject with code: {}", request.getCode());

        // Validate unique code
        if (subjectRepository.existsByCode(request.getCode())) {
            log.error("Subject code already exists: {}", request.getCode());
            throw new CustomException(ErrorCode.SUBJECT_CODE_DUPLICATE);
        }

        // Validate and parse status
        SubjectStatus subjectStatus;
        try {
            subjectStatus = SubjectStatus.valueOf(request.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.error("Invalid status value: {}", request.getStatus());
            throw new CustomException(ErrorCode.INVALID_STATUS);
        }

        // Get current user ID from security context
        Long currentUserId = getCurrentUserId();

        // Create subject entity
        Subject subject = new Subject();
        subject.setCode(request.getCode());
        subject.setName(request.getName());
        subject.setDescription(request.getDescription());
        subject.setStatus(subjectStatus);
        subject.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        // Set createdBy if user ID is available
        if (currentUserId != null) {
            UserAccount createdBy = new UserAccount();
            createdBy.setId(currentUserId);
            subject.setCreatedBy(createdBy);
        }

        Subject savedSubject = subjectRepository.save(subject);

        log.info("Subject created successfully with ID: {}", savedSubject.getId());
        return convertToDTO(savedSubject);
    }

    /**
     * Update an existing subject
     *
     * @param id Subject ID
     * @param request Update data
     * @return Updated subject information
     */
    @Override
    public SubjectDTO updateSubject(Long id, UpdateSubjectRequestDTO request) {
        log.info("Updating subject ID: {}", id);

        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.SUBJECT_NOT_FOUND));

        // Update fields if provided
        if (request.getName() != null && !request.getName().isBlank()) {
            subject.setName(request.getName());
        }

        if (request.getDescription() != null) {
            subject.setDescription(request.getDescription());
        }

        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            try {
                SubjectStatus newStatus = SubjectStatus.valueOf(request.getStatus().toUpperCase());
                subject.setStatus(newStatus);
            } catch (IllegalArgumentException e) {
                log.error("Invalid status value: {}", request.getStatus());
                throw new CustomException(ErrorCode.INVALID_STATUS);
            }
        }

        subject.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        Subject updatedSubject = subjectRepository.save(subject);

        log.info("Subject updated successfully: {}", id);
        return convertToDTO(updatedSubject);
    }

    /**
     * Delete a subject (soft delete by setting status to INACTIVE)
     *
     * @param id Subject ID
     */
    @Override
    public void deleteSubject(Long id) {
        log.info("Deleting subject ID: {}", id);

        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.SUBJECT_NOT_FOUND));

        // Check if subject has levels
        long levelsCount = levelRepository.countBySubjectId(id);
        if (levelsCount > 0) {
            log.error("Cannot delete subject with existing levels. Subject ID: {}, Levels count: {}", id, levelsCount);
            throw new CustomException(ErrorCode.SUBJECT_HAS_LEVELS);
        }

        // Check if subject has courses
        long coursesCount = courseRepository.countBySubjectId(id);
        if (coursesCount > 0) {
            log.error("Cannot delete subject with existing courses. Subject ID: {}, Courses count: {}", id, coursesCount);
            throw new CustomException(ErrorCode.SUBJECT_HAS_COURSES);
        }

        // Soft delete: set status to INACTIVE
        subject.setStatus(SubjectStatus.INACTIVE);
        subject.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        subjectRepository.save(subject);

        log.info("Subject soft deleted successfully: {}", id);
    }

    /**
     * Convert Subject entity to SubjectDTO
     */
    private SubjectDTO convertToDTO(Subject subject) {
        long levelsCount = levelRepository.countBySubjectId(subject.getId());
        long coursesCount = courseRepository.countBySubjectId(subject.getId());

        return SubjectDTO.builder()
                .id(subject.getId())
                .code(subject.getCode())
                .name(subject.getName())
                .description(subject.getDescription())
                .status(subject.getStatus().name())
                .createdBy(subject.getCreatedBy() != null ? subject.getCreatedBy().getId() : null)
                .createdAt(subject.getCreatedAt() != null ? subject.getCreatedAt().toLocalDateTime() : null)
                .levelsCount((int) levelsCount)
                .coursesCount((int) coursesCount)
                .build();
    }

    /**
     * Convert Subject entity to SubjectDetailDTO with levels
     */
    private SubjectDetailDTO convertToDetailDTO(Subject subject, List<Level> levels) {
        long coursesCount = courseRepository.countBySubjectId(subject.getId());

        List<LevelDTO> levelDTOs = levels.stream()
                .map(this::convertLevelToDTO)
                .collect(Collectors.toList());

        return SubjectDetailDTO.builder()
                .id(subject.getId())
                .code(subject.getCode())
                .name(subject.getName())
                .description(subject.getDescription())
                .status(subject.getStatus().name())
                .createdBy(subject.getCreatedBy() != null ? subject.getCreatedBy().getId() : null)
                .createdAt(subject.getCreatedAt() != null ? subject.getCreatedAt().toLocalDateTime() : null)
                .levelsCount(levels.size())
                .coursesCount((int) coursesCount)
                .levels(levelDTOs)
                .build();
    }

    /**
     * Convert Level entity to LevelDTO
     */
    private LevelDTO convertLevelToDTO(Level level) {
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

    /**
     * Get current user ID from security context
     */
    private Long getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof org.fyp.emssep490be.configs.CustomUserDetails) {
                org.fyp.emssep490be.configs.CustomUserDetails userDetails =
                        (org.fyp.emssep490be.configs.CustomUserDetails) authentication.getPrincipal();
                return userDetails.getUserId();
            }
        } catch (Exception e) {
            log.warn("Could not get current user ID from security context", e);
        }
        return null;
    }
}
