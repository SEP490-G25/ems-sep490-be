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

    @GetMapping("/api/v1/courses/{courseId}/clos")
    public ResponseEntity<ResponseObject<List<CloDTO>>> getClosByCourse(@PathVariable Long courseId) {
        // TODO: Implement
        return ResponseEntity.ok(new ResponseObject<>(HttpStatus.OK.value(), "CLOs retrieved successfully", null));
    }

    @PostMapping("/api/v1/courses/{courseId}/clos")
    public ResponseEntity<ResponseObject<CloDTO>> createClo(
            @PathVariable Long courseId,
            @Valid @RequestBody CreateCloRequestDTO request) {
        // TODO: Implement
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ResponseObject<>(HttpStatus.CREATED.value(), "CLO created successfully", null));
    }

    @PostMapping("/api/v1/plos/{ploId}/clos/{cloId}/mapping")
    public ResponseEntity<ResponseObject<Map<String, Object>>> mapPloToClo(
            @PathVariable Long ploId,
            @PathVariable Long cloId,
            @Valid @RequestBody MappingRequestDTO request) {
        // TODO: Implement PLO to CLO mapping
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ResponseObject<>(HttpStatus.CREATED.value(), "PLO mapped to CLO successfully", null));
    }

    @PostMapping("/api/v1/course-sessions/{sessionId}/clos/{cloId}/mapping")
    public ResponseEntity<ResponseObject<Map<String, Object>>> mapCloToSession(
            @PathVariable Long sessionId,
            @PathVariable Long cloId,
            @Valid @RequestBody MappingRequestDTO request) {
        // TODO: Implement CLO to Course Session mapping
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ResponseObject<>(HttpStatus.CREATED.value(), "CLO mapped to session successfully", null));
    }

    @DeleteMapping("/api/v1/courses/{courseId}/clos/{id}")
    public ResponseEntity<Void> deleteClo(@PathVariable Long courseId, @PathVariable Long id) {
        // TODO: Implement
        return ResponseEntity.noContent().build();
    }
}
