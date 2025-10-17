package org.fyp.emssep490be.controllers.course;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fyp.emssep490be.dtos.ResponseObject;
import org.fyp.emssep490be.dtos.common.PagedResponseDTO;
import org.fyp.emssep490be.dtos.course.*;
import org.fyp.emssep490be.services.course.CourseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for Course management operations
 * Base path: /api/v1/courses
 */
@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @GetMapping
    public ResponseEntity<ResponseObject<PagedResponseDTO<CourseDTO>>> getAllCourses(
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long levelId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean approved,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer limit) {
        // TODO: Implement
        return ResponseEntity.ok(new ResponseObject<>(HttpStatus.OK.value(), "Courses retrieved successfully", null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject<CourseDetailDTO>> getCourseById(@PathVariable Long id) {
        // TODO: Implement
        return ResponseEntity.ok(new ResponseObject<>(HttpStatus.OK.value(), "Course retrieved successfully", null));
    }

    @PostMapping
    public ResponseEntity<ResponseObject<CourseDTO>> createCourse(@Valid @RequestBody CreateCourseRequestDTO request) {
        // TODO: Implement
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ResponseObject<>(HttpStatus.CREATED.value(), "Course created successfully", null));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseObject<CourseDTO>> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCourseRequestDTO request) {
        // TODO: Implement
        return ResponseEntity.ok(new ResponseObject<>(HttpStatus.OK.value(), "Course updated successfully", null));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<ResponseObject<CourseDTO>> submitCourseForApproval(@PathVariable Long id) {
        // TODO: Implement
        return ResponseEntity.ok(new ResponseObject<>(HttpStatus.OK.value(), "Course submitted for approval", null));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ResponseObject<CourseDTO>> approveCourse(
            @PathVariable Long id,
            @Valid @RequestBody ApprovalRequestDTO request) {
        // TODO: Implement
        return ResponseEntity.ok(new ResponseObject<>(HttpStatus.OK.value(), "Course approval processed", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        // TODO: Implement
        return ResponseEntity.noContent().build();
    }
}
