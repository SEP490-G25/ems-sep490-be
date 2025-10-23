package org.fyp.emssep490be.controllers.plo;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fyp.emssep490be.dtos.ResponseObject;
import org.fyp.emssep490be.dtos.plo.CreatePloRequestDTO;
import org.fyp.emssep490be.dtos.plo.PloDTO;
import org.fyp.emssep490be.services.plo.PloService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for PLO (Program Learning Outcome) management operations
 * Base path: /api/v1/subjects/{subjectId}/plos
 */
@RestController
@RequestMapping("/api/v1/subjects/{subjectId}/plos")
@RequiredArgsConstructor
public class PloController {

    private final PloService ploService;

    /**
     * Get all PLOs for a subject
     * GET /api/v1/subjects/{subjectId}/plos
     *
     * @param subjectId Subject ID
     * @return List of PLOs with mapped CLOs count
     */
    @GetMapping
    public ResponseEntity<ResponseObject<List<PloDTO>>> getPlosBySubject(@PathVariable Long subjectId) {
        List<PloDTO> plos = ploService.getPlosBySubject(subjectId);
        return ResponseEntity.ok(
                new ResponseObject<>(HttpStatus.OK.value(), "PLOs retrieved successfully", plos));
    }

    /**
     * Create a new PLO for a subject
     * POST /api/v1/subjects/{subjectId}/plos
     *
     * @param subjectId Subject ID
     * @param request PLO creation request
     * @return Created PLO DTO
     */
    @PostMapping
    public ResponseEntity<ResponseObject<PloDTO>> createPlo(
            @PathVariable Long subjectId,
            @Valid @RequestBody CreatePloRequestDTO request) {
        PloDTO createdPlo = ploService.createPlo(subjectId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ResponseObject<>(HttpStatus.CREATED.value(), "PLO created successfully", createdPlo));
    }

    /**
     * Delete a PLO
     * DELETE /api/v1/subjects/{subjectId}/plos/{id}
     *
     * @param subjectId Subject ID
     * @param id PLO ID
     * @return No content response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseObject<Void>> deletePlo(@PathVariable Long subjectId, @PathVariable Long id) {
        ploService.deletePlo(subjectId, id);
        return ResponseEntity.ok(
                new ResponseObject<>(HttpStatus.OK.value(), "PLO deleted successfully", null));
    }
}
