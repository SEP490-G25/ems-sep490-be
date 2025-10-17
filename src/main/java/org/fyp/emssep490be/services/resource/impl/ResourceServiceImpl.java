package org.fyp.emssep490be.services.resource.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fyp.emssep490be.dtos.resource.CreateResourceRequestDTO;
import org.fyp.emssep490be.dtos.resource.ResourceDTO;
import org.fyp.emssep490be.dtos.resource.UpdateResourceRequestDTO;
import org.fyp.emssep490be.repositories.ResourceRepository;
import org.fyp.emssep490be.services.resource.ResourceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Implementation of ResourceService for Resource management operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ResourceServiceImpl implements ResourceService {

    private final ResourceRepository resourceRepository;

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
        // TODO: Implement get resources by branch
        // - Apply filters (resourceType)
        // - Check availability by querying SessionResource for conflicts
        // - Convert to DTOs
        log.info("Getting resources for branch ID: {}, type: {}", branchId, resourceType);
        return null;
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
        // TODO: Implement get resource by ID
        // - Fetch resource
        // - Validate branch ownership
        // - Convert to DTO
        log.info("Getting resource ID: {} for branch ID: {}", id, branchId);
        return null;
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
        // TODO: Implement create resource
        // - Validate branch exists
        // - Validate resource type specific fields
        // - Create and save resource entity
        // - Convert to DTO
        log.info("Creating resource for branch ID: {}, type: {}", branchId, request.getResourceType());
        return null;
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
        // TODO: Implement update resource
        // - Find existing resource
        // - Validate branch ownership
        // - Update fields
        // - Save and convert to DTO
        log.info("Updating resource ID: {} for branch ID: {}", id, branchId);
        return null;
    }

    /**
     * Delete a resource
     *
     * @param branchId Branch ID
     * @param id       Resource ID
     */
    @Override
    public void deleteResource(Long branchId, Long id) {
        // TODO: Implement delete resource
        // - Check if resource is in use by any sessions
        // - Delete resource
        log.info("Deleting resource ID: {} for branch ID: {}", id, branchId);
    }
}
