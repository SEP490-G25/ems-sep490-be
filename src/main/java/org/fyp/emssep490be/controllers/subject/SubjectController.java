package org.fyp.emssep490be.controllers.subject;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fyp.emssep490be.dtos.ResponseObject;
import org.fyp.emssep490be.dtos.common.PagedResponseDTO;
import org.fyp.emssep490be.dtos.subject.CreateSubjectRequestDTO;
import org.fyp.emssep490be.dtos.subject.SubjectDTO;
import org.fyp.emssep490be.dtos.subject.UpdateSubjectRequestDTO;
import org.fyp.emssep490be.services.subject.SubjectService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for Subject management operations
 * Base path: /api/v1/subjects
 */
@RestController
@RequestMapping("/api/v1/subjects")
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectService subjectService;

    /**
     * Get All Subjects
     * GET /subjects
     *
     * @param status Filter by status (active|inactive)
     * @param page   Page number
     * @param limit  Items per page
     * @return Paginated list of subjects
     */
    @GetMapping
    public ResponseEntity<ResponseObject<PagedResponseDTO<SubjectDTO>>> getAllSubjects(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer limit) {
        // TODO: Implement get all subjects logic
        return ResponseEntity.ok(
                new ResponseObject<>(HttpStatus.OK.value(), "Subjects retrieved successfully", null)
        );
    }

    /**
     * Get Subject by ID
     * GET /subjects/{id}
     *
     * @param id Subject ID
     * @return Subject information
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject<SubjectDTO>> getSubjectById(@PathVariable Long id) {
        // TODO: Implement get subject by ID logic
        return ResponseEntity.ok(
                new ResponseObject<>(HttpStatus.OK.value(), "Subject retrieved successfully", null)
        );
    }

    /**
     * Create Subject
     * POST /subjects
     * Roles: SUBJECT_LEADER
     *
     * @param request Subject creation data
     * @return Created subject information
     */
    @PostMapping
    public ResponseEntity<ResponseObject<SubjectDTO>> createSubject(
            @Valid @RequestBody CreateSubjectRequestDTO request) {
        // TODO: Implement create subject logic
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ResponseObject<>(HttpStatus.CREATED.value(), "Subject created successfully", null)
        );
    }

    /**
     * Update Subject
     * PUT /subjects/{id}
     * Roles: SUBJECT_LEADER
     *
     * @param id      Subject ID
     * @param request Subject update data
     * @return Updated subject information
     */
    @PutMapping("/{id}")
    public ResponseEntity<ResponseObject<SubjectDTO>> updateSubject(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSubjectRequestDTO request) {
        // TODO: Implement update subject logic
        return ResponseEntity.ok(
                new ResponseObject<>(HttpStatus.OK.value(), "Subject updated successfully", null)
        );
    }

    /**
     * Delete Subject
     * DELETE /subjects/{id}
     * Roles: SUBJECT_LEADER
     *
     * @param id Subject ID
     * @return No content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubject(@PathVariable Long id) {
        // TODO: Implement delete subject logic
        return ResponseEntity.noContent().build();
    }
}
