package org.fyp.emssep490be.services.resource;

import org.fyp.emssep490be.dtos.resource.CreateResourceRequestDTO;
import org.fyp.emssep490be.dtos.resource.ResourceDTO;
import org.fyp.emssep490be.dtos.resource.UpdateResourceRequestDTO;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Service interface for Resource (Rooms & Virtual) management operations
 */
public interface ResourceService {

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
    List<ResourceDTO> getResourcesByBranch(Long branchId, String resourceType,
                                           LocalDate availableDate, LocalTime availableStartTime,
                                           LocalTime availableEndTime);

    /**
     * Get resource by ID
     *
     * @param branchId Branch ID
     * @param id       Resource ID
     * @return Resource information
     */
    ResourceDTO getResourceById(Long branchId, Long id);

    /**
     * Create a new resource for a branch
     *
     * @param branchId Branch ID
     * @param request  Resource creation data
     * @return Created resource information
     */
    ResourceDTO createResource(Long branchId, CreateResourceRequestDTO request);

    /**
     * Update an existing resource
     *
     * @param branchId Branch ID
     * @param id       Resource ID
     * @param request  Resource update data
     * @return Updated resource information
     */
    ResourceDTO updateResource(Long branchId, Long id, UpdateResourceRequestDTO request);

    /**
     * Delete a resource
     *
     * @param branchId Branch ID
     * @param id       Resource ID
     */
    void deleteResource(Long branchId, Long id);
}
