package org.fyp.emssep490be.controllers.resource;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fyp.emssep490be.dtos.ResponseObject;
import org.fyp.emssep490be.dtos.resource.CreateResourceRequestDTO;
import org.fyp.emssep490be.dtos.resource.ResourceDTO;
import org.fyp.emssep490be.dtos.resource.UpdateResourceRequestDTO;
import org.fyp.emssep490be.services.resource.ResourceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Controller for Resource (Rooms & Virtual) management operations
 * Base path: /api/v1/branches/{branchId}/resources
 */
@RestController
@RequestMapping("/api/v1/branches/{branchId}/resources")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

    /**
     * Get Resources by Branch
     * GET /branches/{branchId}/resources
     *
     * @param branchId            Branch ID
     * @param resourceType        Filter by resource type (ROOM|VIRTUAL)
     * @param availableDate       Filter by available date (YYYY-MM-DD)
     * @param availableStartTime  Filter by available start time (HH:MM:SS)
     * @param availableEndTime    Filter by available end time (HH:MM:SS)
     * @return List of resources for the branch
     */
    @GetMapping
    public ResponseEntity<ResponseObject<List<ResourceDTO>>> getResourcesByBranch(
            @PathVariable Long branchId,
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) LocalDate availableDate,
            @RequestParam(required = false) LocalTime availableStartTime,
            @RequestParam(required = false) LocalTime availableEndTime) {
        List<ResourceDTO> result = resourceService.getResourcesByBranch(
                branchId, resourceType, availableDate, availableStartTime, availableEndTime);
        return ResponseEntity.ok(
                new ResponseObject<>(HttpStatus.OK.value(), "Resources retrieved successfully", result)
        );
    }

    /**
     * Get Resource by ID
     * GET /branches/{branchId}/resources/{id}
     *
     * @param branchId   Branch ID
     * @param id         Resource ID
     * @return Resource information
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject<ResourceDTO>> getResourceById(
            @PathVariable Long branchId,
            @PathVariable Long id) {
        ResourceDTO result = resourceService.getResourceById(branchId, id);
        return ResponseEntity.ok(
                new ResponseObject<>(HttpStatus.OK.value(), "Resource retrieved successfully", result)
        );
    }

    /**
     * Create Resource
     * POST /branches/{branchId}/resources
     * Roles: MANAGER, CENTER_HEAD
     *
     * @param branchId Branch ID
     * @param request  Resource creation data (ROOM or VIRTUAL)
     * @return Created resource information
     */
    @PostMapping
    public ResponseEntity<ResponseObject<ResourceDTO>> createResource(
            @PathVariable Long branchId,
            @Valid @RequestBody CreateResourceRequestDTO request) {
        ResourceDTO result = resourceService.createResource(branchId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ResponseObject<>(HttpStatus.CREATED.value(), "Resource created successfully", result)
        );
    }

    /**
     * Update Resource
     * PUT /branches/{branchId}/resources/{id}
     * Roles: MANAGER, CENTER_HEAD
     *
     * @param branchId Branch ID
     * @param id       Resource ID
     * @param request  Resource update data
     * @return Updated resource information
     */
    @PutMapping("/{id}")
    public ResponseEntity<ResponseObject<ResourceDTO>> updateResource(
            @PathVariable Long branchId,
            @PathVariable Long id,
            @Valid @RequestBody UpdateResourceRequestDTO request) {
        ResourceDTO result = resourceService.updateResource(branchId, id, request);
        return ResponseEntity.ok(
                new ResponseObject<>(HttpStatus.OK.value(), "Resource updated successfully", result)
        );
    }

    /**
     * Delete Resource
     * DELETE /branches/{branchId}/resources/{id}
     * Roles: MANAGER, CENTER_HEAD
     *
     * @param branchId Branch ID
     * @param id       Resource ID
     * @return No content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResource(
            @PathVariable Long branchId,
            @PathVariable Long id) {
        resourceService.deleteResource(branchId, id);
        return ResponseEntity.noContent().build();
    }
}
