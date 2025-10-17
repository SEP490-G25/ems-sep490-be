package org.fyp.emssep490be.controllers.student;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fyp.emssep490be.dtos.ResponseObject;
import org.fyp.emssep490be.dtos.enrollment.CreateEnrollmentRequestDTO;
import org.fyp.emssep490be.dtos.enrollment.EnrollmentDTO;
import org.fyp.emssep490be.dtos.student.StudentProfileDTO;
import org.fyp.emssep490be.services.student.StudentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for Student operations
 * Base path: /api/v1/students
 */
@RestController
@RequestMapping("/api/v1/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject<StudentProfileDTO>> getStudentProfile(@PathVariable Long id) {
        // TODO: Implement
        return ResponseEntity.ok(new ResponseObject<>(HttpStatus.OK.value(), "Student profile retrieved", null));
    }

    @GetMapping("/{id}/enrollments")
    public ResponseEntity<ResponseObject<List<EnrollmentDTO>>> getStudentEnrollments(@PathVariable Long id) {
        // TODO: Implement
        return ResponseEntity.ok(new ResponseObject<>(HttpStatus.OK.value(), "Enrollments retrieved", null));
    }

    @PostMapping("/{id}/enrollments")
    public ResponseEntity<ResponseObject<EnrollmentDTO>> enrollStudent(
            @PathVariable Long id,
            @Valid @RequestBody CreateEnrollmentRequestDTO request) {
        // TODO: Implement enrollment with auto-generation of StudentSession records
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ResponseObject<>(HttpStatus.CREATED.value(), "Student enrolled successfully", null));
    }

    @GetMapping("/{id}/schedule")
    public ResponseEntity<ResponseObject<Object>> getStudentSchedule(@PathVariable Long id) {
        // TODO: Implement student schedule
        return ResponseEntity.ok(new ResponseObject<>(HttpStatus.OK.value(), "Schedule retrieved", null));
    }
}
