package org.fyp.emssep490be.services.branch.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fyp.emssep490be.dtos.branch.BranchDTO;
import org.fyp.emssep490be.dtos.branch.BranchDetailDTO;
import org.fyp.emssep490be.dtos.branch.CreateBranchRequestDTO;
import org.fyp.emssep490be.dtos.branch.UpdateBranchRequestDTO;
import org.fyp.emssep490be.dtos.common.PagedResponseDTO;
import org.fyp.emssep490be.dtos.common.PaginationDTO;
import org.fyp.emssep490be.dtos.resource.ResourceDTO;
import org.fyp.emssep490be.dtos.timeslot.TimeSlotDTO;
import org.fyp.emssep490be.entities.Branch;
import org.fyp.emssep490be.entities.Center;
import org.fyp.emssep490be.entities.Resource;
import org.fyp.emssep490be.entities.TimeSlotTemplate;
import org.fyp.emssep490be.entities.enums.BranchStatus;
import org.fyp.emssep490be.exceptions.CustomException;
import org.fyp.emssep490be.exceptions.ErrorCode;
import org.fyp.emssep490be.repositories.BranchRepository;
import org.fyp.emssep490be.repositories.CenterRepository;
import org.fyp.emssep490be.repositories.ResourceRepository;
import org.fyp.emssep490be.repositories.TimeSlotTemplateRepository;
import org.fyp.emssep490be.services.branch.BranchService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of BranchService for Branch management operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BranchServiceImpl implements BranchService {

    private final BranchRepository branchRepository;
    private final CenterRepository centerRepository;
    private final TimeSlotTemplateRepository timeSlotTemplateRepository;
    private final ResourceRepository resourceRepository;

    /**
     * Get all branches with pagination and filtering
     *
     * @param centerId Filter by center ID
     * @param status   Filter by status
     * @param page     Page number
     * @param limit    Items per page
     * @return Paginated list of branches
     */
    @Override
    @Transactional(readOnly = true)
    public PagedResponseDTO<BranchDTO> getAllBranches(Long centerId, String status, Integer page, Integer limit) {
        log.info("Getting all branches: centerId={}, status={}, page={}, limit={}", centerId, status, page, limit);

        // Set default pagination values
        int pageNumber = (page != null && page > 0) ? page - 1 : 0;
        int pageSize = (limit != null && limit > 0) ? limit : 10;

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("id").descending());

        Page<Branch> branchPage;

        // Apply filters
        if (centerId != null && status != null && !status.isBlank()) {
            branchPage = branchRepository.findByCenterIdAndStatus(centerId, status, pageable);
        } else if (centerId != null) {
            branchPage = branchRepository.findByCenterId(centerId, pageable);
        } else if (status != null && !status.isBlank()) {
            branchPage = branchRepository.findByStatus(status, pageable);
        } else {
            branchPage = branchRepository.findAll(pageable);
        }

        List<BranchDTO> branchDTOs = branchPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        PaginationDTO pagination = new PaginationDTO(
                branchPage.getNumber() + 1,
                branchPage.getTotalPages(),
                branchPage.getTotalElements(),
                branchPage.getSize()
        );

        return new PagedResponseDTO<>(branchDTOs, pagination);
    }

    /**
     * Get branch by ID with detailed information
     *
     * @param id Branch ID
     * @return Branch details including time slots and resources
     */
    @Override
    @Transactional(readOnly = true)
    public BranchDetailDTO getBranchById(Long id) {
        log.info("Getting branch by ID: {}", id);

        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.BRANCH_NOT_FOUND));

        // Fetch related time slots and resources
        List<TimeSlotTemplate> timeSlots = timeSlotTemplateRepository.findByBranchIdOrderByStartTimeAsc(id);
        List<Resource> resources = resourceRepository.findByBranchId(id);

        return convertToDetailDTO(branch, timeSlots, resources);
    }

    /**
     * Create a new branch
     *
     * @param request Branch creation data
     * @return Created branch information
     */
    @Override
    public BranchDTO createBranch(CreateBranchRequestDTO request) {
        log.info("Creating branch: {}", request.getCode());

        // Validate center exists
        Center center = centerRepository.findById(request.getCenterId())
                .orElseThrow(() -> new CustomException(ErrorCode.CENTER_NOT_FOUND));

        // Validate unique constraint (center_id + code)
        if (branchRepository.existsByCodeAndCenterId(request.getCode(), request.getCenterId())) {
            throw new CustomException(ErrorCode.BRANCH_CODE_ALREADY_EXISTS);
        }

        // Validate and parse status
        BranchStatus branchStatus;
        try {
            branchStatus = (request.getStatus() != null && !request.getStatus().isBlank())
                    ? BranchStatus.valueOf(request.getStatus().toUpperCase())
                    : BranchStatus.ACTIVE;
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_STATUS);
        }

        // Create branch entity
        Branch branch = new Branch();
        branch.setCenter(center);
        branch.setCode(request.getCode());
        branch.setName(request.getName());
        branch.setAddress(request.getAddress());
        branch.setLocation(request.getLocation());
        branch.setPhone(request.getPhone());
        branch.setCapacity(request.getCapacity());
        branch.setStatus(branchStatus);
        branch.setOpeningDate(request.getOpeningDate());
        branch.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        branch.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        Branch savedBranch = branchRepository.save(branch);
        log.info("Branch created successfully with ID: {}", savedBranch.getId());

        return convertToDTO(savedBranch);
    }

    /**
     * Update an existing branch
     *
     * @param id      Branch ID
     * @param request Branch update data
     * @return Updated branch information
     */
    @Override
    public BranchDTO updateBranch(Long id, UpdateBranchRequestDTO request) {
        log.info("Updating branch ID: {}", id);

        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.BRANCH_NOT_FOUND));

        // Update fields if provided
        if (request.getName() != null && !request.getName().isBlank()) {
            branch.setName(request.getName());
        }
        if (request.getAddress() != null && !request.getAddress().isBlank()) {
            branch.setAddress(request.getAddress());
        }
        if (request.getLocation() != null) {
            branch.setLocation(request.getLocation());
        }
        if (request.getPhone() != null) {
            branch.setPhone(request.getPhone());
        }
        if (request.getCapacity() != null) {
            branch.setCapacity(request.getCapacity());
        }
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            try {
                branch.setStatus(BranchStatus.valueOf(request.getStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new CustomException(ErrorCode.INVALID_STATUS);
            }
        }
        if (request.getOpeningDate() != null) {
            branch.setOpeningDate(request.getOpeningDate());
        }

        branch.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        Branch updatedBranch = branchRepository.save(branch);
        log.info("Branch updated successfully: {}", updatedBranch.getId());

        return convertToDTO(updatedBranch);
    }

    /**
     * Delete a branch
     *
     * @param id Branch ID
     */
    @Override
    public void deleteBranch(Long id) {
        log.info("Deleting branch ID: {}", id);

        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.BRANCH_NOT_FOUND));

        // Soft delete - set status to CLOSED
        branch.setStatus(BranchStatus.CLOSED);
        branch.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        branchRepository.save(branch);

        log.info("Branch soft deleted (status set to CLOSED): {}", id);
    }

    // ============ Private Helper Methods ============

    /**
     * Convert Branch entity to BranchDTO
     */
    private BranchDTO convertToDTO(Branch branch) {
        BranchDTO dto = new BranchDTO();
        dto.setId(branch.getId());
        dto.setCenterId(branch.getCenter().getId());
        dto.setCode(branch.getCode());
        dto.setName(branch.getName());
        dto.setAddress(branch.getAddress());
        dto.setLocation(branch.getLocation());
        dto.setPhone(branch.getPhone());
        dto.setCapacity(branch.getCapacity());
        dto.setStatus(branch.getStatus().name());
        dto.setOpeningDate(branch.getOpeningDate());
        dto.setCreatedAt(branch.getCreatedAt() != null ? branch.getCreatedAt().toLocalDateTime() : null);
        dto.setUpdatedAt(branch.getUpdatedAt() != null ? branch.getUpdatedAt().toLocalDateTime() : null);
        return dto;
    }

    /**
     * Convert Branch entity with related data to BranchDetailDTO
     */
    private BranchDetailDTO convertToDetailDTO(Branch branch, List<TimeSlotTemplate> timeSlots, List<Resource> resources) {
        BranchDetailDTO dto = new BranchDetailDTO();
        dto.setId(branch.getId());
        dto.setCenterId(branch.getCenter().getId());
        dto.setCode(branch.getCode());
        dto.setName(branch.getName());
        dto.setAddress(branch.getAddress());
        dto.setLocation(branch.getLocation());
        dto.setPhone(branch.getPhone());
        dto.setCapacity(branch.getCapacity());
        dto.setStatus(branch.getStatus().name());
        dto.setOpeningDate(branch.getOpeningDate());

        // Convert time slots
        List<TimeSlotDTO> timeSlotDTOs = timeSlots.stream()
                .map(this::convertTimeSlotToDTO)
                .collect(Collectors.toList());
        dto.setTimeSlots(timeSlotDTOs);

        // Convert resources
        List<ResourceDTO> resourceDTOs = resources.stream()
                .map(this::convertResourceToDTO)
                .collect(Collectors.toList());
        dto.setResources(resourceDTOs);

        dto.setCreatedAt(branch.getCreatedAt() != null ? branch.getCreatedAt().toLocalDateTime() : null);
        dto.setUpdatedAt(branch.getUpdatedAt() != null ? branch.getUpdatedAt().toLocalDateTime() : null);

        return dto;
    }

    /**
     * Convert TimeSlotTemplate entity to TimeSlotDTO
     */
    private TimeSlotDTO convertTimeSlotToDTO(TimeSlotTemplate timeSlot) {
        TimeSlotDTO dto = new TimeSlotDTO();
        dto.setId(timeSlot.getId());
        dto.setBranchId(timeSlot.getBranch().getId());
        dto.setName(timeSlot.getName());
        dto.setStartTime(timeSlot.getStartTime());
        dto.setEndTime(timeSlot.getEndTime());
        dto.setDurationMinutes(timeSlot.getDurationMinutes());
        dto.setCreatedAt(timeSlot.getCreatedAt() != null ? timeSlot.getCreatedAt().toLocalDateTime() : null);
        dto.setUpdatedAt(timeSlot.getUpdatedAt() != null ? timeSlot.getUpdatedAt().toLocalDateTime() : null);
        return dto;
    }

    /**
     * Convert Resource entity to ResourceDTO
     */
    private ResourceDTO convertResourceToDTO(Resource resource) {
        ResourceDTO dto = new ResourceDTO();
        dto.setId(resource.getId());
        dto.setBranchId(resource.getBranch().getId());
        dto.setResourceType(resource.getResourceType().name());
        dto.setName(resource.getName());
        dto.setLocation(resource.getLocation());
        dto.setCapacity(resource.getCapacity());
        dto.setDescription(resource.getDescription());
        dto.setEquipment(resource.getEquipment());
        dto.setMeetingUrl(resource.getMeetingUrl());
        dto.setMeetingId(resource.getMeetingId());
        dto.setAccountEmail(resource.getAccountEmail());
        dto.setLicenseType(resource.getLicenseType());
        dto.setExpiryDate(resource.getExpiryDate());
        dto.setRenewalDate(resource.getRenewalDate());
        dto.setCreatedBy(resource.getCreatedBy() != null ? resource.getCreatedBy().getId() : null);
        dto.setCreatedAt(resource.getCreatedAt() != null ? resource.getCreatedAt().toLocalDateTime() : null);
        dto.setUpdatedAt(resource.getUpdatedAt() != null ? resource.getUpdatedAt().toLocalDateTime() : null);
        return dto;
    }
}
