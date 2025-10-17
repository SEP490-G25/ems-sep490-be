package org.fyp.emssep490be.controllers.classmanagement;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fyp.emssep490be.dtos.ResponseObject;
import org.fyp.emssep490be.dtos.classmanagement.*;
import org.fyp.emssep490be.dtos.common.PagedResponseDTO;
import org.fyp.emssep490be.dtos.course.ApprovalRequestDTO;
import org.fyp.emssep490be.services.classmanagement.ClassManagementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Controller for Class Management operations
 * Base path: /api/v1/classes
 */
@RestController
@RequestMapping("/api/v1/classes")
@RequiredArgsConstructor
public class ClassManagementController {

    private final ClassManagementService classManagementService;

    /**
     * Get All Classes
     * GET /classes
     *
     * @param branchId      Filter by branch ID
     * @param courseId      Filter by course ID
     * @param status        Filter by status (draft|scheduled|ongoing|completed|cancelled)
     * @param modality      Filter by modality (OFFLINE|ONLINE|HYBRID)
     * @param startDateFrom Filter by start date from
     * @param startDateTo   Filter by start date to
     * @param page          Page number
     * @param limit         Items per page
     * @return Paginated list of classes
     */
    @GetMapping
    public ResponseEntity<ResponseObject<PagedResponseDTO<ClassDTO>>> getAllClasses(
            @RequestParam(required = false) Long branchId,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String modality,
            @RequestParam(required = false) LocalDate startDateFrom,
            @RequestParam(required = false) LocalDate startDateTo,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer limit) {
        // TODO: Implement get all classes logic
        return ResponseEntity.ok(
                new ResponseObject<>(HttpStatus.OK.value(), "Classes retrieved successfully", null)
        );
    }

    /**
     * Get Class Detail
     * GET /classes/{id}
     *
     * @param id Class ID
     * @return Detailed class information
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject<ClassDetailDTO>> getClassById(@PathVariable Long id) {
        // TODO: Implement get class by ID logic
        return ResponseEntity.ok(
                new ResponseObject<>(HttpStatus.OK.value(), "Class retrieved successfully", null)
        );
    }

    /**
     * Create Class
     * POST /classes
     * Roles: MANAGER, ACADEMIC_STAFF
     * Note: Auto-generates sessions from course template
     *
     * @param request Class creation data
     * @return Created class information with sessions count
     */
    @PostMapping
    public ResponseEntity<ResponseObject<CreateClassResponseDTO>> createClass(
            @Valid @RequestBody CreateClassRequestDTO request) {
        // TODO: Implement create class logic
        // - Validate course is approved
        // - Validate branch and course exist
        // - Create class entity
        // - Auto-generate sessions from course template
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ResponseObject<>(HttpStatus.CREATED.value(), "Class created successfully", null)
        );
    }

    /**
     * Submit Class for Approval
     * POST /classes/{id}/submit
     * Roles: ACADEMIC_STAFF
     *
     * @param id Class ID
     * @return Submitted class information
     */
    @PostMapping("/{id}/submit")
    public ResponseEntity<ResponseObject<ClassDTO>> submitClassForApproval(@PathVariable Long id) {
        // TODO: Implement submit class logic
        return ResponseEntity.ok(
                new ResponseObject<>(HttpStatus.OK.value(), "Class submitted for approval", null)
        );
    }

    /**
     * Approve/Reject Class
     * POST /classes/{id}/approve
     * Roles: MANAGER, CENTER_HEAD
     *
     * @param id      Class ID
     * @param request Approval action (approve/reject) with optional rejection reason
     * @return Approved/rejected class information
     */
    @PostMapping("/{id}/approve")
    public ResponseEntity<ResponseObject<ClassDTO>> approveClass(
            @PathVariable Long id,
            @Valid @RequestBody ApprovalRequestDTO request) {
        // TODO: Implement approve/reject class logic
        return ResponseEntity.ok(
                new ResponseObject<>(HttpStatus.OK.value(), "Class approval processed", null)
        );
    }

    /**
     * Update Class Schedule
     * PUT /classes/{id}/schedule
     * Roles: MANAGER
     * Updates schedule for future sessions based on effective date
     *
     * @param id      Class ID
     * @param request Schedule update data (effective date, target day, new slot)
     * @return Update result with sessions count and conflicts
     */
    @PutMapping("/{id}/schedule")
    public ResponseEntity<ResponseObject<UpdateClassScheduleResponseDTO>> updateClassSchedule(
            @PathVariable Long id,
            @Valid @RequestBody UpdateClassScheduleRequestDTO request) {
        // TODO: Implement update class schedule logic
        // - Update future sessions from effective date
        // - Detect and report conflicts
        // - Update related StudentSession records
        return ResponseEntity.ok(
                new ResponseObject<>(HttpStatus.OK.value(), "Schedule updated successfully", null)
        );
    }

    /**
     * Validate Class Schedule
     * POST /classes/{id}/validate
     * Roles: MANAGER, ACADEMIC_STAFF
     * Checks for resource conflicts, teacher conflicts, and capacity issues
     *
     * @param id Class ID
     * @return Validation result with conflicts and warnings
     */
    @PostMapping("/{id}/validate")
    public ResponseEntity<ResponseObject<ClassValidationResponseDTO>> validateClassSchedule(@PathVariable Long id) {
        // TODO: Implement class validation logic
        // - Check resource conflicts
        // - Check teacher conflicts
        // - Check capacity warnings
        return ResponseEntity.ok(
                new ResponseObject<>(HttpStatus.OK.value(), "Validation completed", null)
        );
    }

    /**
     * Delete Class
     * DELETE /classes/{id}
     * Roles: MANAGER
     *
     * @param id Class ID
     * @return No content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClass(@PathVariable Long id) {
        // TODO: Implement delete class logic
        return ResponseEntity.noContent().build();
    }
}
