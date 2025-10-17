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

    @GetMapping
    public ResponseEntity<ResponseObject<List<PloDTO>>> getPlosBySubject(@PathVariable Long subjectId) {
        // TODO: Implement
        return ResponseEntity.ok(new ResponseObject<>(HttpStatus.OK.value(), "PLOs retrieved successfully", null));
    }

    @PostMapping
    public ResponseEntity<ResponseObject<PloDTO>> createPlo(
            @PathVariable Long subjectId,
            @Valid @RequestBody CreatePloRequestDTO request) {
        // TODO: Implement
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ResponseObject<>(HttpStatus.CREATED.value(), "PLO created successfully", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlo(@PathVariable Long subjectId, @PathVariable Long id) {
        // TODO: Implement
        return ResponseEntity.noContent().build();
    }
}
