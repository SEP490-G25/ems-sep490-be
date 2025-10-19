package org.fyp.emssep490be.services.resource.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fyp.emssep490be.dtos.resource.CreateResourceRequestDTO;
import org.fyp.emssep490be.dtos.resource.ResourceDTO;
import org.fyp.emssep490be.dtos.resource.UpdateResourceRequestDTO;
import org.fyp.emssep490be.entities.Branch;
import org.fyp.emssep490be.entities.Resource;
import org.fyp.emssep490be.entities.enums.ResourceType;
import org.fyp.emssep490be.exceptions.CustomException;
import org.fyp.emssep490be.exceptions.ErrorCode;
import org.fyp.emssep490be.repositories.BranchRepository;
import org.fyp.emssep490be.repositories.ResourceRepository;
import org.fyp.emssep490be.services.resource.ResourceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of ResourceService for Resource management operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ResourceServiceImpl implements ResourceService {

    private final ResourceRepository resourceRepository;
    private final BranchRepository branchRepository;

    /**
     * Get all resources for a branch with optional availability filtering
     *
     * @param branchId           Branch ID
     * @param resourceType       Filter by resource type (ROOM|VIRTUAL)
     * @param availableDate      Filter by available date
     * @param availableStartTime Filter by available start time
     * @param availableEndTime   Filter by available end time
     * @return List of resources
     */
    @Override
    @Transactional(readOnly = true)
    public List<ResourceDTO> getResourcesByBranch(Long branchId, String resourceType,
                                                   LocalDate availableDate, LocalTime availableStartTime,
                                                   LocalTime availableEndTime) {
        log.info("Getting resources for branch ID: {}, type: {}, date: {}, time: {}-{}",
                branchId, resourceType, availableDate, availableStartTime, availableEndTime);

        List<Resource> resources;

        // If availability filter is provided, use conflict detection algorithm
        if (availableDate != null && availableStartTime != null && availableEndTime != null) {
            // CORE ALGORITHM: Find available resources (no conflicts)
            resources = resourceRepository.findAvailableResources(
                    branchId,
                    resourceType,
                    availableDate,
                    availableStartTime,
                    availableEndTime
            );
        } else if (resourceType != null && !resourceType.isBlank()) {
            // Filter by type only
            resources = resourceRepository.findByBranchIdAndResourceType(branchId, resourceType);
        } else {
            // Get all resources for branch
            resources = resourceRepository.findByBranchId(branchId);
        }

        return resources.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get resource by ID
     *
     * @param branchId Branch ID
     * @param id       Resource ID
     * @return Resource information
     */
    @Override
    @Transactional(readOnly = true)
    public ResourceDTO getResourceById(Long branchId, Long id) {
        log.info("Getting resource ID: {} for branch ID: {}", id, branchId);

        Resource resource = resourceRepository.findByIdAndBranchId(id, branchId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        return convertToDTO(resource);
    }

    /**
     * Create a new resource for a branch
     *
     * @param branchId Branch ID
     * @param request  Resource creation data
     * @return Created resource information
     */
    @Override
    public ResourceDTO createResource(Long branchId, CreateResourceRequestDTO request) {
        log.info("Creating resource for branch ID: {}, type: {}", branchId, request.getResourceType());

        // Validate branch exists
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new CustomException(ErrorCode.BRANCH_NOT_FOUND));

        // Validate and parse resource type
        ResourceType resourceTypeEnum;
        try {
            resourceTypeEnum = ResourceType.valueOf(request.getResourceType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.RESOURCE_INVALID_TYPE);
        }

        // Validate unique resource name per branch
        if (resourceRepository.existsByBranchIdAndName(branchId, request.getName())) {
            throw new CustomException(ErrorCode.RESOURCE_NAME_ALREADY_EXISTS);
        }

        // Create resource entity
        Resource resource = new Resource();
        resource.setBranch(branch);
        resource.setResourceType(resourceTypeEnum);
        resource.setName(request.getName());
        resource.setDescription(request.getDescription());

        // Type-specific fields
        if (resourceTypeEnum == ResourceType.ROOM) {
            resource.setLocation(request.getLocation());
            resource.setCapacity(request.getCapacity());
            resource.setEquipment(request.getEquipment());
        } else if (resourceTypeEnum == ResourceType.VIRTUAL) {
            resource.setMeetingUrl(request.getMeetingUrl());
            resource.setMeetingId(request.getMeetingId());
            resource.setAccountEmail(request.getAccountEmail());
            resource.setLicenseType(request.getLicenseType());
            resource.setExpiryDate(request.getExpiryDate());
        }

        resource.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        resource.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        Resource savedResource = resourceRepository.save(resource);
        log.info("Resource created successfully with ID: {}", savedResource.getId());

        return convertToDTO(savedResource);
    }

    /**
     * Update an existing resource
     *
     * @param branchId Branch ID
     * @param id       Resource ID
     * @param request  Resource update data
     * @return Updated resource information
     */
    @Override
    public ResourceDTO updateResource(Long branchId, Long id, UpdateResourceRequestDTO request) {
        log.info("Updating resource ID: {} for branch ID: {}", id, branchId);

        Resource resource = resourceRepository.findByIdAndBranchId(id, branchId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        // Update common fields
        if (request.getName() != null && !request.getName().isBlank()) {
            resource.setName(request.getName());
        }
        if (request.getDescription() != null) {
            resource.setDescription(request.getDescription());
        }

        // Update type-specific fields based on resource type
        if (resource.getResourceType() == ResourceType.ROOM) {
            if (request.getLocation() != null) {
                resource.setLocation(request.getLocation());
            }
            if (request.getCapacity() != null) {
                resource.setCapacity(request.getCapacity());
            }
            if (request.getEquipment() != null) {
                resource.setEquipment(request.getEquipment());
            }
        } else if (resource.getResourceType() == ResourceType.VIRTUAL) {
            if (request.getMeetingUrl() != null) {
                resource.setMeetingUrl(request.getMeetingUrl());
            }
            if (request.getMeetingId() != null) {
                resource.setMeetingId(request.getMeetingId());
            }
            if (request.getAccountEmail() != null) {
                resource.setAccountEmail(request.getAccountEmail());
            }
            if (request.getLicenseType() != null) {
                resource.setLicenseType(request.getLicenseType());
            }
            if (request.getExpiryDate() != null) {
                resource.setExpiryDate(request.getExpiryDate());
            }
        }

        resource.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        Resource updatedResource = resourceRepository.save(resource);
        log.info("Resource updated successfully: {}", updatedResource.getId());

        return convertToDTO(updatedResource);
    }

    /**
     * Delete a resource
     *
     * @param branchId Branch ID
     * @param id       Resource ID
     */
    @Override
    public void deleteResource(Long branchId, Long id) {
        log.info("Deleting resource ID: {} for branch ID: {}", id, branchId);

        Resource resource = resourceRepository.findByIdAndBranchId(id, branchId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        // Note: In production, you should check if this resource is in use by any sessions
        // For now, we'll allow deletion

        resourceRepository.delete(resource);
        log.info("Resource deleted successfully: {}", id);
    }

    // ============ Private Helper Methods ============

    /**
     * Convert Resource entity to ResourceDTO
     */
    private ResourceDTO convertToDTO(Resource resource) {
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
