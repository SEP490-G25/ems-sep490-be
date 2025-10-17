package org.fyp.emssep490be.controllers.coursesession;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fyp.emssep490be.dtos.ResponseObject;
import org.fyp.emssep490be.dtos.coursesession.CourseSessionDTO;
import org.fyp.emssep490be.dtos.coursesession.CreateCourseSessionRequestDTO;
import org.fyp.emssep490be.services.coursesession.CourseSessionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for Course Session (Template) management operations
 * Base path: /api/v1/phases/{phaseId}/sessions
 */
@RestController
@RequestMapping("/api/v1/phases/{phaseId}/sessions")
@RequiredArgsConstructor
public class CourseSessionController {

    private final CourseSessionService courseSessionService;

    @GetMapping
    public ResponseEntity<ResponseObject<List<CourseSessionDTO>>> getSessionsByPhase(@PathVariable Long phaseId) {
        // TODO: Implement
        return ResponseEntity.ok(new ResponseObject<>(HttpStatus.OK.value(), "Sessions retrieved successfully", null));
    }

    @PostMapping
    public ResponseEntity<ResponseObject<CourseSessionDTO>> createSession(
            @PathVariable Long phaseId,
            @Valid @RequestBody CreateCourseSessionRequestDTO request) {
        // TODO: Implement
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ResponseObject<>(HttpStatus.CREATED.value(), "Session created successfully", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSession(@PathVariable Long phaseId, @PathVariable Long id) {
        // TODO: Implement
        return ResponseEntity.noContent().build();
    }
}
