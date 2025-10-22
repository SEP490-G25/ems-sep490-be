package org.fyp.emssep490be.controllers.subject;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fyp.emssep490be.dtos.ResponseObject;
import org.fyp.emssep490be.dtos.common.PagedResponseDTO;
import org.fyp.emssep490be.dtos.subject.CreateSubjectRequestDTO;
import org.fyp.emssep490be.dtos.subject.SubjectDTO;
import org.fyp.emssep490be.dtos.subject.SubjectDetailDTO;
import org.fyp.emssep490be.dtos.subject.UpdateSubjectRequestDTO;
import org.fyp.emssep490be.services.subject.SubjectService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for Subject management operations
 * Base path: /api/v1/subjects
 */
@RestController
@RequestMapping("/api/v1/subjects")
@RequiredArgsConstructor
@Validated
@Tag(name = "Subjects", description = "Subject management APIs")
public class SubjectController {

    private final SubjectService subjectService;

    /**
     * Get All Subjects
     * GET /api/v1/subjects
     *
     * @param status Filter by status (ACTIVE|INACTIVE)
     * @param page   Page number (1-based)
     * @param limit  Items per page
     * @return Paginated list of subjects
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CENTER_HEAD', 'ACADEMIC_STAFF', 'SUBJECT_LEADER')")
    @Operation(summary = "Get all subjects", description = "Retrieve list of subjects with optional filtering by status")
    public ResponseEntity<ResponseObject<PagedResponseDTO<SubjectDTO>>> getAllSubjects(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer limit) {

        PagedResponseDTO<SubjectDTO> subjects = subjectService.getAllSubjects(status, page, limit);

        return ResponseEntity.ok(
                ResponseObject.<PagedResponseDTO<SubjectDTO>>builder()
                        .status(HttpStatus.OK.value())
                        .message("Subjects retrieved successfully")
                        .data(subjects)
                        .build()
        );
    }

    /**
     * Get Subject by ID
     * GET /api/v1/subjects/{id}
     *
     * @param id Subject ID
     * @return Subject detailed information with levels
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CENTER_HEAD', 'ACADEMIC_STAFF', 'SUBJECT_LEADER')")
    @Operation(summary = "Get subject by ID", description = "Retrieve detailed subject information including related levels")
    public ResponseEntity<ResponseObject<SubjectDetailDTO>> getSubjectById(@PathVariable Long id) {

        SubjectDetailDTO subject = subjectService.getSubjectById(id);

        return ResponseEntity.ok(
                ResponseObject.<SubjectDetailDTO>builder()
                        .status(HttpStatus.OK.value())
                        .message("Subject retrieved successfully")
                        .data(subject)
                        .build()
        );
    }

    /**
     * Create Subject
     * POST /api/v1/subjects
     * Roles: ADMIN, SUBJECT_LEADER
     *
     * @param request Subject creation data
     * @return Created subject information
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUBJECT_LEADER')")
    @Operation(summary = "Create subject", description = "Create a new subject (requires ADMIN or SUBJECT_LEADER role)")
    public ResponseEntity<ResponseObject<SubjectDTO>> createSubject(
            @Valid @RequestBody CreateSubjectRequestDTO request) {

        SubjectDTO createdSubject = subjectService.createSubject(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ResponseObject.<SubjectDTO>builder()
                        .status(HttpStatus.CREATED.value())
                        .message("Subject created successfully")
                        .data(createdSubject)
                        .build()
        );
    }

    /**
     * Update Subject
     * PUT /api/v1/subjects/{id}
     * Roles: ADMIN, SUBJECT_LEADER
     *
     * @param id      Subject ID
     * @param request Subject update data
     * @return Updated subject information
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUBJECT_LEADER')")
    @Operation(summary = "Update subject", description = "Update an existing subject (requires ADMIN or SUBJECT_LEADER role)")
    public ResponseEntity<ResponseObject<SubjectDTO>> updateSubject(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSubjectRequestDTO request) {

        SubjectDTO updatedSubject = subjectService.updateSubject(id, request);

        return ResponseEntity.ok(
                ResponseObject.<SubjectDTO>builder()
                        .status(HttpStatus.OK.value())
                        .message("Subject updated successfully")
                        .data(updatedSubject)
                        .build()
        );
    }

    /**
     * Delete Subject
     * DELETE /api/v1/subjects/{id}
     * Roles: ADMIN
     *
     * @param id Subject ID
     * @return Success message
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete subject", description = "Soft delete a subject by setting status to INACTIVE (requires ADMIN role)")
    public ResponseEntity<ResponseObject<Void>> deleteSubject(@PathVariable Long id) {

        subjectService.deleteSubject(id);

        return ResponseEntity.ok(
                ResponseObject.<Void>builder()
                        .status(HttpStatus.OK.value())
                        .message("Subject deleted successfully")
                        .data(null)
                        .build()
        );
    }
}
