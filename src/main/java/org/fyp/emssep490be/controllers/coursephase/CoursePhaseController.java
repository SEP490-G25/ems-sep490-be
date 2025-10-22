package org.fyp.emssep490be.controllers.coursephase;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fyp.emssep490be.dtos.ResponseObject;
import org.fyp.emssep490be.dtos.coursephase.CoursePhaseDTO;
import org.fyp.emssep490be.dtos.coursephase.CreateCoursePhaseRequestDTO;
import org.fyp.emssep490be.dtos.coursephase.UpdateCoursePhaseRequestDTO;
import org.fyp.emssep490be.services.coursephase.CoursePhaseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for Course Phase management operations
 * Handles CRUD operations for course phases
 * Base path: /api/v1/courses/{courseId}/phases and /api/v1/phases/{id}
 */
@RestController
@RequiredArgsConstructor
@Validated
@Tag(name = "Course Phases", description = "Course phase management APIs")
public class CoursePhaseController {

    private final CoursePhaseService coursePhaseService;

    /**
     * Get all phases for a course
     * GET /api/v1/courses/{courseId}/phases
     *
     * @param courseId Course ID
     * @return List of course phases
     */
    @GetMapping("/api/v1/courses/{courseId}/phases")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CENTER_HEAD', 'ACADEMIC_STAFF', 'SUBJECT_LEADER')")
    @Operation(summary = "Get all phases for a course", description = "Retrieve list of phases for a specific course ordered by sort_order")
    public ResponseEntity<ResponseObject<List<CoursePhaseDTO>>> getPhasesByCourse(@PathVariable Long courseId) {

        List<CoursePhaseDTO> phases = coursePhaseService.getPhasesByCourse(courseId);

        return ResponseEntity.ok(
                ResponseObject.<List<CoursePhaseDTO>>builder()
                        .status(HttpStatus.OK.value())
                        .message("Phases retrieved successfully")
                        .data(phases)
                        .build()
        );
    }

    /**
     * Create a new phase for a course
     * POST /api/v1/courses/{courseId}/phases
     *
     * @param courseId Course ID
     * @param request  Phase creation request
     * @return Created phase information
     */
    @PostMapping("/api/v1/courses/{courseId}/phases")
    @PreAuthorize("hasAnyRole('SUBJECT_LEADER', 'ADMIN')")
    @Operation(summary = "Create a new phase", description = "Create a new phase for a course (requires SUBJECT_LEADER or ADMIN role)")
    public ResponseEntity<ResponseObject<CoursePhaseDTO>> createPhase(
            @PathVariable Long courseId,
            @Valid @RequestBody CreateCoursePhaseRequestDTO request) {

        CoursePhaseDTO createdPhase = coursePhaseService.createPhase(courseId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ResponseObject.<CoursePhaseDTO>builder()
                        .status(HttpStatus.CREATED.value())
                        .message("Phase created successfully")
                        .data(createdPhase)
                        .build()
        );
    }

    /**
     * Update an existing phase
     * PUT /api/v1/phases/{id}
     *
     * @param id      Phase ID
     * @param request Phase update request
     * @return Updated phase information
     */
    @PutMapping("/api/v1/phases/{id}")
    @PreAuthorize("hasAnyRole('SUBJECT_LEADER', 'ADMIN')")
    @Operation(summary = "Update a phase", description = "Update an existing phase (only allowed for draft courses)")
    public ResponseEntity<ResponseObject<CoursePhaseDTO>> updatePhase(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCoursePhaseRequestDTO request) {

        CoursePhaseDTO updatedPhase = coursePhaseService.updatePhase(id, request);

        return ResponseEntity.ok(
                ResponseObject.<CoursePhaseDTO>builder()
                        .status(HttpStatus.OK.value())
                        .message("Phase updated successfully")
                        .data(updatedPhase)
                        .build()
        );
    }

    /**
     * Delete a phase
     * DELETE /api/v1/phases/{id}
     *
     * @param id Phase ID
     * @return No content
     */
    @DeleteMapping("/api/v1/phases/{id}")
    @PreAuthorize("hasAnyRole('SUBJECT_LEADER', 'ADMIN')")
    @Operation(summary = "Delete a phase", description = "Delete a phase (only allowed if no course sessions exist)")
    public ResponseEntity<ResponseObject<Void>> deletePhase(@PathVariable Long id) {

        coursePhaseService.deletePhase(id);

        return ResponseEntity.ok(
                ResponseObject.<Void>builder()
                        .status(HttpStatus.OK.value())
                        .message("Phase deleted successfully")
                        .build()
        );
    }
}
