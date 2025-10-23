package org.fyp.emssep490be.controllers.clo;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fyp.emssep490be.dtos.ResponseObject;
import org.fyp.emssep490be.dtos.clo.CloDTO;
import org.fyp.emssep490be.dtos.clo.CreateCloRequestDTO;
import org.fyp.emssep490be.dtos.clo.MappingRequestDTO;
import org.fyp.emssep490be.services.clo.CloService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller for CLO (Course Learning Outcome) management and mapping operations
 * Base paths: /api/v1/courses/{courseId}/clos, /api/v1/plos/{ploId}/clos/{cloId}/mapping, etc.
 */
@RestController
@RequiredArgsConstructor
public class CloController {

    private final CloService cloService;

    /**
     * Get all CLOs for a course with mapped PLOs
     * GET /api/v1/courses/{courseId}/clos
     *
     * @param courseId Course ID
     * @return List of CLOs with mapped PLOs
     */
    @GetMapping("/api/v1/courses/{courseId}/clos")
    public ResponseEntity<ResponseObject<List<CloDTO>>> getClosByCourse(@PathVariable Long courseId) {
        List<CloDTO> clos = cloService.getClosByCourse(courseId);
        return ResponseEntity.ok(
                new ResponseObject<>(HttpStatus.OK.value(), "CLOs retrieved successfully", clos));
    }

    /**
     * Create a new CLO for a course
     * POST /api/v1/courses/{courseId}/clos
     *
     * @param courseId Course ID
     * @param request CLO creation request
     * @return Created CLO DTO
     */
    @PostMapping("/api/v1/courses/{courseId}/clos")
    public ResponseEntity<ResponseObject<CloDTO>> createClo(
            @PathVariable Long courseId,
            @Valid @RequestBody CreateCloRequestDTO request) {
        CloDTO createdClo = cloService.createClo(courseId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ResponseObject<>(HttpStatus.CREATED.value(), "CLO created successfully", createdClo));
    }

    /**
     * Map a PLO to a CLO
     * POST /api/v1/plos/{ploId}/clos/{cloId}/mapping
     * CRITICAL: Validates that PLO and CLO belong to the same subject
     *
     * @param ploId PLO ID
     * @param cloId CLO ID
     * @param request Mapping request with optional status
     * @return Mapping result with PLO and CLO details
     */
    @PostMapping("/api/v1/plos/{ploId}/clos/{cloId}/mapping")
    public ResponseEntity<ResponseObject<Map<String, Object>>> mapPloToClo(
            @PathVariable Long ploId,
            @PathVariable Long cloId,
            @Valid @RequestBody MappingRequestDTO request) {
        Map<String, Object> mapping = cloService.mapPloToClo(ploId, cloId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ResponseObject<>(HttpStatus.CREATED.value(), "PLO mapped to CLO successfully", mapping));
    }

    /**
     * Map a CLO to a CourseSession
     * POST /api/v1/course-sessions/{sessionId}/clos/{cloId}/mapping
     * CRITICAL: Validates that CLO and CourseSession belong to the same course
     *
     * @param sessionId CourseSession ID
     * @param cloId CLO ID
     * @param request Mapping request with optional status
     * @return Mapping result with CLO and CourseSession details
     */
    @PostMapping("/api/v1/course-sessions/{sessionId}/clos/{cloId}/mapping")
    public ResponseEntity<ResponseObject<Map<String, Object>>> mapCloToSession(
            @PathVariable Long sessionId,
            @PathVariable Long cloId,
            @Valid @RequestBody MappingRequestDTO request) {
        Map<String, Object> mapping = cloService.mapCloToSession(sessionId, cloId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ResponseObject<>(HttpStatus.CREATED.value(), "CLO mapped to session successfully", mapping));
    }

    /**
     * Delete a CLO
     * DELETE /api/v1/courses/{courseId}/clos/{id}
     * Validates that CLO has no existing PLO or session mappings
     *
     * @param courseId Course ID
     * @param id CLO ID
     * @return Success response
     */
    @DeleteMapping("/api/v1/courses/{courseId}/clos/{id}")
    public ResponseEntity<ResponseObject<Void>> deleteClo(@PathVariable Long courseId, @PathVariable Long id) {
        cloService.deleteClo(courseId, id);
        return ResponseEntity.ok(
                new ResponseObject<>(HttpStatus.OK.value(), "CLO deleted successfully", null));
    }
}
