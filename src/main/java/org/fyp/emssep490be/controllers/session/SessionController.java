package org.fyp.emssep490be.controllers.session;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fyp.emssep490be.dtos.ResponseObject;
import org.fyp.emssep490be.dtos.common.PagedResponseDTO;
import org.fyp.emssep490be.dtos.session.*;
import org.fyp.emssep490be.services.session.SessionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller for Session management operations
 * Base paths: /api/v1/classes/{classId}/sessions, /api/v1/sessions/{id}
 */
@RestController
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @GetMapping("/api/v1/classes/{classId}/sessions")
    public ResponseEntity<ResponseObject<PagedResponseDTO<SessionDTO>>> getSessionsByClass(
            @PathVariable Long classId,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer limit) {
        // TODO: Implement
        return ResponseEntity.ok(new ResponseObject<>(HttpStatus.OK.value(), "Sessions retrieved successfully", null));
    }

    @GetMapping("/api/v1/sessions/{id}")
    public ResponseEntity<ResponseObject<SessionDetailDTO>> getSessionById(@PathVariable Long id) {
        // TODO: Implement
        return ResponseEntity.ok(new ResponseObject<>(HttpStatus.OK.value(), "Session retrieved successfully", null));
    }

    @PutMapping("/api/v1/sessions/{id}")
    public ResponseEntity<ResponseObject<SessionDTO>> updateSession(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSessionRequestDTO request) {
        // TODO: Implement
        return ResponseEntity.ok(new ResponseObject<>(HttpStatus.OK.value(), "Session updated successfully", null));
    }

    @PostMapping("/api/v1/sessions/{id}/cancel")
    public ResponseEntity<ResponseObject<CancelSessionResponseDTO>> cancelSession(
            @PathVariable Long id,
            @Valid @RequestBody CancelSessionRequestDTO request) {
        // TODO: Implement
        return ResponseEntity.ok(new ResponseObject<>(HttpStatus.OK.value(), "Session cancelled successfully", null));
    }

    @PostMapping("/api/v1/sessions/{sessionId}/teachers")
    public ResponseEntity<ResponseObject<AssignTeacherResponseDTO>> assignTeacher(
            @PathVariable Long sessionId,
            @Valid @RequestBody AssignTeacherRequestDTO request) {
        // TODO: Implement
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ResponseObject<>(HttpStatus.CREATED.value(), "Teacher assigned successfully", null));
    }

    @GetMapping("/api/v1/sessions/{sessionId}/available-teachers")
    public ResponseEntity<ResponseObject<List<AvailableTeacherDTO>>> getAvailableTeachers(
            @PathVariable Long sessionId,
            @RequestParam(required = false) String skill) {
        // TODO: Implement
        return ResponseEntity.ok(new ResponseObject<>(HttpStatus.OK.value(), "Available teachers retrieved successfully", null));
    }

    @DeleteMapping("/api/v1/sessions/{sessionId}/teachers/{teacherId}")
    public ResponseEntity<Void> removeTeacher(
            @PathVariable Long sessionId,
            @PathVariable Long teacherId,
            @RequestParam(required = false) String skill) {
        // TODO: Implement
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/v1/sessions/{sessionId}/resources")
    public ResponseEntity<ResponseObject<AssignResourceResponseDTO>> assignResource(
            @PathVariable Long sessionId,
            @Valid @RequestBody AssignResourceRequestDTO request) {
        // TODO: Implement
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ResponseObject<>(HttpStatus.CREATED.value(), "Resource assigned successfully", null));
    }

    @GetMapping("/api/v1/sessions/{sessionId}/available-resources")
    public ResponseEntity<ResponseObject<List<AvailableResourceDTO>>> getAvailableResources(
            @PathVariable Long sessionId,
            @RequestParam(required = false) String resourceType) {
        // TODO: Implement
        return ResponseEntity.ok(new ResponseObject<>(HttpStatus.OK.value(), "Available resources retrieved successfully", null));
    }

    @DeleteMapping("/api/v1/sessions/{sessionId}/resources/{resourceId}")
    public ResponseEntity<Void> removeResource(
            @PathVariable Long sessionId,
            @PathVariable Long resourceId) {
        // TODO: Implement
        return ResponseEntity.noContent().build();
    }
}
