package org.fyp.emssep490be.controllers.coursephase;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fyp.emssep490be.dtos.ResponseObject;
import org.fyp.emssep490be.dtos.coursephase.CoursePhaseDTO;
import org.fyp.emssep490be.dtos.coursephase.CreateCoursePhaseRequestDTO;
import org.fyp.emssep490be.services.coursephase.CoursePhaseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for Course Phase management operations
 * Base path: /api/v1/courses/{courseId}/phases
 */
@RestController
@RequestMapping("/api/v1/courses/{courseId}/phases")
@RequiredArgsConstructor
public class CoursePhaseController {

    private final CoursePhaseService coursePhaseService;

    @GetMapping
    public ResponseEntity<ResponseObject<List<CoursePhaseDTO>>> getPhasesByCourse(@PathVariable Long courseId) {
        // TODO: Implement
        return ResponseEntity.ok(new ResponseObject<>(HttpStatus.OK.value(), "Phases retrieved successfully", null));
    }

    @PostMapping
    public ResponseEntity<ResponseObject<CoursePhaseDTO>> createPhase(
            @PathVariable Long courseId,
            @Valid @RequestBody CreateCoursePhaseRequestDTO request) {
        // TODO: Implement
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ResponseObject<>(HttpStatus.CREATED.value(), "Phase created successfully", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePhase(@PathVariable Long courseId, @PathVariable Long id) {
        // TODO: Implement
        return ResponseEntity.noContent().build();
    }
}
