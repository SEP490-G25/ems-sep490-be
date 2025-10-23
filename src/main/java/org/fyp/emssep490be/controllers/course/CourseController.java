package org.fyp.emssep490be.controllers.course;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fyp.emssep490be.dtos.ResponseObject;
import org.fyp.emssep490be.dtos.common.PagedResponseDTO;
import org.fyp.emssep490be.dtos.course.*;
import org.fyp.emssep490be.services.course.CourseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for Course management operations
 * Handles CRUD operations and approval workflow for courses
 * Base path: /api/v1/courses
 */
@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
@Validated
@Tag(name = "Courses", description = "Course management and approval workflow APIs")
public class CourseController {

    private final CourseService courseService;

    /**
     * Get all courses with pagination and filtering
     * GET /api/v1/courses
     *
     * @param subjectId Filter by subject ID
     * @param levelId   Filter by level ID
     * @param status    Filter by status
     * @param approved  Filter by approval status
     * @param page      Page number (1-based)
     * @param limit     Items per page
     * @return Paginated list of courses
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CENTER_HEAD', 'ACADEMIC_STAFF', 'SUBJECT_LEADER')")
    @Operation(summary = "Get all courses", description = "Retrieve list of courses with optional filtering")
    public ResponseEntity<ResponseObject<PagedResponseDTO<CourseDTO>>> getAllCourses(
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long levelId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean approved,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer limit) {

        PagedResponseDTO<CourseDTO> courses = courseService.getAllCourses(subjectId, levelId, status, approved, page, limit);

        return ResponseEntity.ok(
                ResponseObject.<PagedResponseDTO<CourseDTO>>builder()
                        .status(HttpStatus.OK.value())
                        .message("Courses retrieved successfully")
                        .data(courses)
                        .build()
        );
    }

    /**
     * Get course by ID with detailed information
     * GET /api/v1/courses/{id}
     *
     * @param id Course ID
     * @return Course detailed information including phases, CLOs, and materials
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CENTER_HEAD', 'ACADEMIC_STAFF', 'SUBJECT_LEADER')")
    @Operation(summary = "Get course by ID", description = "Retrieve detailed course information including phases and related data")
    public ResponseEntity<ResponseObject<CourseDetailDTO>> getCourseById(@PathVariable Long id) {

        CourseDetailDTO course = courseService.getCourseById(id);

        return ResponseEntity.ok(
                ResponseObject.<CourseDetailDTO>builder()
                        .status(HttpStatus.OK.value())
                        .message("Course retrieved successfully")
                        .data(course)
                        .build()
        );
    }

    /**
     * Create a new course
     * POST /api/v1/courses
     *
     * @param request Course creation request
     * @return Created course information
     */
    @PostMapping
    @PreAuthorize("hasRole('SUBJECT_LEADER')")
    @Operation(summary = "Create course", description = "Create a new course (requires SUBJECT_LEADER role)")
    public ResponseEntity<ResponseObject<CourseDTO>> createCourse(@Valid @RequestBody CreateCourseRequestDTO request) {

        CourseDTO createdCourse = courseService.createCourse(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ResponseObject.<CourseDTO>builder()
                        .status(HttpStatus.CREATED.value())
                        .message("Course created successfully")
                        .data(createdCourse)
                        .build()
        );
    }

    /**
     * Update an existing course
     * PUT /api/v1/courses/{id}
     *
     * @param id      Course ID
     * @param request Course update request
     * @return Updated course information
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUBJECT_LEADER')")
    @Operation(summary = "Update course", description = "Update an existing course (only allowed for draft or rejected courses)")
    public ResponseEntity<ResponseObject<CourseDTO>> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCourseRequestDTO request) {

        CourseDTO updatedCourse = courseService.updateCourse(id, request);

        return ResponseEntity.ok(
                ResponseObject.<CourseDTO>builder()
                        .status(HttpStatus.OK.value())
                        .message("Course updated successfully")
                        .data(updatedCourse)
                        .build()
        );
    }

    /**
     * Submit course for approval
     * POST /api/v1/courses/{id}/submit
     *
     * @param id Course ID
     * @return Updated course information
     */
    @PostMapping("/{id}/submit")
    @PreAuthorize("hasRole('SUBJECT_LEADER')")
    @Operation(summary = "Submit course for approval", description = "Submit a course for manager approval")
    public ResponseEntity<ResponseObject<CourseDTO>> submitCourseForApproval(@PathVariable Long id) {

        CourseDTO submittedCourse = courseService.submitCourseForApproval(id);

        return ResponseEntity.ok(
                ResponseObject.<CourseDTO>builder()
                        .status(HttpStatus.OK.value())
                        .message("Course submitted for approval successfully")
                        .data(submittedCourse)
                        .build()
        );
    }

    /**
     * Approve or reject a course
     * POST /api/v1/courses/{id}/approve
     *
     * @param id      Course ID
     * @param request Approval request (action: approve/reject, rejection reason if reject)
     * @return Updated course information
     */
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('MANAGER', 'CENTER_HEAD')")
    @Operation(summary = "Approve or reject course", description = "Approve or reject a submitted course (requires MANAGER or CENTER_HEAD role)")
    public ResponseEntity<ResponseObject<CourseDTO>> approveCourse(
            @PathVariable Long id,
            @Valid @RequestBody ApprovalRequestDTO request) {

        CourseDTO approvedCourse = courseService.approveCourse(id, request);

        return ResponseEntity.ok(
                ResponseObject.<CourseDTO>builder()
                        .status(HttpStatus.OK.value())
                        .message("Course approval processed successfully")
                        .data(approvedCourse)
                        .build()
        );
    }

    /**
     * Delete a course (soft delete)
     * DELETE /api/v1/courses/{id}
     *
     * @param id Course ID
     * @return No content
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUBJECT_LEADER')")
    @Operation(summary = "Delete course", description = "Delete a course (soft delete, only allowed if not used by any classes)")
    public ResponseEntity<ResponseObject<Void>> deleteCourse(@PathVariable Long id) {

        courseService.deleteCourse(id);

        return ResponseEntity.ok(
                ResponseObject.<Void>builder()
                        .status(HttpStatus.OK.value())
                        .message("Course deleted successfully")
                        .build()
        );
    }
}
