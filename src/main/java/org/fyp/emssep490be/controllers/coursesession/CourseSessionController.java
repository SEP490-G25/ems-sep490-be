package org.fyp.emssep490be.controllers.coursesession;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fyp.emssep490be.dtos.ResponseObject;
import org.fyp.emssep490be.dtos.coursesession.CourseSessionDTO;
import org.fyp.emssep490be.dtos.coursesession.CreateCourseSessionRequestDTO;
import org.fyp.emssep490be.dtos.coursesession.UpdateCourseSessionRequestDTO;
import org.fyp.emssep490be.services.coursesession.CourseSessionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for Course Session (Template) management operations
 * Handles CRUD operations for course sessions
 * Base path: /api/v1/phases/{phaseId}/sessions and /api/v1/course-sessions/{id}
 */
@RestController
@RequiredArgsConstructor
@Validated
@Tag(name = "Course Sessions", description = "Course session (template) management APIs")
public class CourseSessionController {

    private final CourseSessionService courseSessionService;

    /**
     * Get all sessions for a phase
     * GET /api/v1/phases/{phaseId}/sessions
     *
     * @param phaseId Phase ID
     * @return List of course sessions
     */
    @GetMapping("/api/v1/phases/{phaseId}/sessions")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CENTER_HEAD', 'ACADEMIC_STAFF', 'SUBJECT_LEADER')")
    @Operation(summary = "Get all sessions for a phase", description = "Retrieve list of sessions for a specific phase ordered by sequence_no")
    public ResponseEntity<ResponseObject<List<CourseSessionDTO>>> getSessionsByPhase(@PathVariable Long phaseId) {

        List<CourseSessionDTO> sessions = courseSessionService.getSessionsByPhase(phaseId);

        return ResponseEntity.ok(
                ResponseObject.<List<CourseSessionDTO>>builder()
                        .status(HttpStatus.OK.value())
                        .message("Sessions retrieved successfully")
                        .data(sessions)
                        .build()
        );
    }

    /**
     * Create a new session for a phase
     * POST /api/v1/phases/{phaseId}/sessions
     *
     * @param phaseId Phase ID
     * @param request Session creation request
     * @return Created session information
     */
    @PostMapping("/api/v1/phases/{phaseId}/sessions")
    @PreAuthorize("hasAnyRole('SUBJECT_LEADER', 'ADMIN')")
    @Operation(summary = "Create a new session", description = "Create a new session for a phase (requires SUBJECT_LEADER or ADMIN role)")
    public ResponseEntity<ResponseObject<CourseSessionDTO>> createSession(
            @PathVariable Long phaseId,
            @Valid @RequestBody CreateCourseSessionRequestDTO request) {

        CourseSessionDTO createdSession = courseSessionService.createSession(phaseId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ResponseObject.<CourseSessionDTO>builder()
                        .status(HttpStatus.CREATED.value())
                        .message("Session created successfully")
                        .data(createdSession)
                        .build()
        );
    }

    /**
     * Update an existing session
     * PUT /api/v1/course-sessions/{id}
     *
     * @param id      Session ID
     * @param request Session update request
     * @return Updated session information
     */
    @PutMapping("/api/v1/course-sessions/{id}")
    @PreAuthorize("hasAnyRole('SUBJECT_LEADER', 'ADMIN')")
    @Operation(summary = "Update a session", description = "Update an existing session (only allowed for draft courses)")
    public ResponseEntity<ResponseObject<CourseSessionDTO>> updateSession(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCourseSessionRequestDTO request) {

        CourseSessionDTO updatedSession = courseSessionService.updateSession(id, request);

        return ResponseEntity.ok(
                ResponseObject.<CourseSessionDTO>builder()
                        .status(HttpStatus.OK.value())
                        .message("Session updated successfully")
                        .data(updatedSession)
                        .build()
        );
    }

    /**
     * Delete a session
     * DELETE /api/v1/course-sessions/{id}
     *
     * @param id Session ID
     * @return No content
     */
    @DeleteMapping("/api/v1/course-sessions/{id}")
    @PreAuthorize("hasAnyRole('SUBJECT_LEADER', 'ADMIN')")
    @Operation(summary = "Delete a session", description = "Delete a session (only allowed if not used in actual sessions)")
    public ResponseEntity<ResponseObject<Void>> deleteSession(@PathVariable Long id) {

        courseSessionService.deleteSession(id);

        return ResponseEntity.ok(
                ResponseObject.<Void>builder()
                        .status(HttpStatus.OK.value())
                        .message("Session deleted successfully")
                        .build()
        );
    }
}
