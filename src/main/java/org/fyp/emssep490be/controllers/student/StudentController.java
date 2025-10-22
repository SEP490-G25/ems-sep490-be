package org.fyp.emssep490be.controllers.student;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fyp.emssep490be.dtos.ResponseObject;
import org.fyp.emssep490be.dtos.enrollment.CreateEnrollmentRequestDTO;
import org.fyp.emssep490be.dtos.enrollment.EnrollmentDTO;
import org.fyp.emssep490be.dtos.student.*;
import org.fyp.emssep490be.services.student.StudentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    /**
     * Create a new student (ACADEMIC_STAFF)
     * POST /api/v1/students
     */
    @PostMapping
    public ResponseEntity<ResponseObject<StudentDTO>> createStudent(
            @Valid @RequestBody CreateStudentRequestDTO request) {
        StudentDTO student = studentService.createStudent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ResponseObject<>(HttpStatus.CREATED.value(), "Student created successfully", student));
    }

    /**
     * Update student information (ACADEMIC_STAFF)
     * PUT /api/v1/students/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ResponseObject<StudentDTO>> updateStudent(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStudentRequestDTO request) {
        StudentDTO student = studentService.updateStudent(id, request);
        return ResponseEntity.ok(
                new ResponseObject<>(HttpStatus.OK.value(), "Student updated successfully", student));
    }

    /**
     * Bulk import students from CSV file (ACADEMIC_STAFF)
     * POST /api/v1/students/import
     */
    @PostMapping("/import")
    public ResponseEntity<ResponseObject<BulkImportStudentResponseDTO>> bulkImportStudents(
            @RequestParam("file") MultipartFile file,
            @RequestParam("branch_id") Long branchId) {
        BulkImportStudentResponseDTO result = studentService.bulkImportStudents(file, branchId);
        return ResponseEntity.ok(
                new ResponseObject<>(HttpStatus.OK.value(), "Bulk import completed", result));
    }

    /**
     * Get student profile
     * GET /api/v1/students/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject<StudentProfileDTO>> getStudentProfile(@PathVariable Long id) {
        StudentProfileDTO profile = studentService.getStudentProfile(id);
        return ResponseEntity.ok(
                new ResponseObject<>(HttpStatus.OK.value(), "Student profile retrieved", profile));
    }

    /**
     * Get student enrollments
     * GET /api/v1/students/{id}/enrollments
     */
    @GetMapping("/{id}/enrollments")
    public ResponseEntity<ResponseObject<List<EnrollmentDTO>>> getStudentEnrollments(@PathVariable Long id) {
        List<EnrollmentDTO> enrollments = studentService.getStudentEnrollments(id);
        return ResponseEntity.ok(
                new ResponseObject<>(HttpStatus.OK.value(), "Enrollments retrieved", enrollments));
    }

    /**
     * Get student schedule
     * GET /api/v1/students/{id}/schedule
     */
    @GetMapping("/{id}/schedule")
    public ResponseEntity<ResponseObject<Object>> getStudentSchedule(@PathVariable Long id) {
        Object schedule = studentService.getStudentSchedule(id);
        return ResponseEntity.ok(
                new ResponseObject<>(HttpStatus.OK.value(), "Schedule retrieved", schedule));
    }
}
