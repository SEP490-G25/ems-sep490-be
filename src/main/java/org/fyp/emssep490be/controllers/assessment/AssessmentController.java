package org.fyp.emssep490be.controllers.assessment;

import lombok.RequiredArgsConstructor;
import org.fyp.emssep490be.dtos.ResponseObject;
import org.fyp.emssep490be.services.assessment.AssessmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for Assessment and Scores operations
 * Base paths: /api/v1/classes/{classId}/assessments, /api/v1/assessments/{assessmentId}/scores
 */
@RestController
@RequiredArgsConstructor
public class AssessmentController {

    private final AssessmentService assessmentService;

    @GetMapping("/api/v1/classes/{classId}/assessments")
    public ResponseEntity<ResponseObject<Object>> getAssessmentsByClass(@PathVariable Long classId) {
        // TODO: Implement
        return ResponseEntity.ok(new ResponseObject<>(HttpStatus.OK.value(), "Assessments retrieved", null));
    }

    @PostMapping("/api/v1/classes/{classId}/assessments")
    public ResponseEntity<ResponseObject<Object>> createAssessment(@PathVariable Long classId) {
        // TODO: Implement
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ResponseObject<>(HttpStatus.CREATED.value(), "Assessment created", null));
    }

    @PostMapping("/api/v1/assessments/{assessmentId}/scores")
    public ResponseEntity<ResponseObject<Object>> recordScores(@PathVariable Long assessmentId) {
        // TODO: Implement
        return ResponseEntity.ok(new ResponseObject<>(HttpStatus.OK.value(), "Scores recorded", null));
    }
}
