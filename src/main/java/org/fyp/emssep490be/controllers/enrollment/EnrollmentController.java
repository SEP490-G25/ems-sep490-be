package org.fyp.emssep490be.controllers.enrollment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fyp.emssep490be.dtos.ResponseObject;
import org.fyp.emssep490be.dtos.enrollment.*;
import org.fyp.emssep490be.services.enrollment.EnrollmentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for enrollment management operations
 * Handles student enrollment, unenrollment, and enrollment queries
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Enrollment Management", description = "APIs for managing student enrollments in classes")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    /**
     * Batch enroll multiple students to a class
     * Supports partial success - returns detailed report of successful and failed enrollments
     * 
     * POST /api/v1/classes/{classId}/enrollments/batch
     */
    @PostMapping("/classes/{classId}/enrollments/batch")
    @Operation(
            summary = "Batch enroll students to a class",
            description = "Academic Staff can add multiple students to a class at once. " +
                    "Validates capacity, checks for duplicates, and auto-generates StudentSession records. " +
                    "Returns detailed success/failure information for each student."
    )
    public ResponseEntity<ResponseObject<BatchEnrollResponseDTO>> batchEnrollStudents(
            @Parameter(description = "Class ID to enroll students into", required = true)
            @PathVariable Long classId,
            
            @Parameter(description = "List of student IDs to enroll", required = true)
            @Valid @RequestBody List<Long> studentIds) {
        
        log.info("Received batch enrollment request for class ID: {} with {} students", 
                classId, studentIds.size());

        BatchEnrollResponseDTO response = enrollmentService.batchEnrollStudents(classId, studentIds);

        String message = String.format("Successfully enrolled %d/%d students", 
                response.getSuccessfulCount(), response.getTotalRequested());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseObject<>(HttpStatus.CREATED.value(), message, response));
    }

    /**
     * Alternative endpoint: Batch enroll with request body containing classId
     * 
     * POST /api/v1/enrollments/batch
     */
    @PostMapping("/enrollments/batch")
    @Operation(
            summary = "Batch enroll students (alternative endpoint)",
            description = "Same as /classes/{classId}/enrollments/batch but with classId in request body"
    )
    public ResponseEntity<ResponseObject<BatchEnrollResponseDTO>> batchEnrollStudentsAlt(
            @Valid @RequestBody BatchEnrollRequestDTO request) {
        
        log.info("Received batch enrollment request for class ID: {} with {} students", 
                request.getClassId(), request.getStudentIds().size());

        BatchEnrollResponseDTO response = enrollmentService.batchEnrollStudents(
                request.getClassId(), 
                request.getStudentIds()
        );

        String message = String.format("Successfully enrolled %d/%d students", 
                response.getSuccessfulCount(), response.getTotalRequested());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseObject<>(HttpStatus.CREATED.value(), message, response));
    }

    /**
     * Get all enrollments for a specific class with pagination
     * 
     * GET /api/v1/classes/{classId}/enrollments
     */
    @GetMapping("/classes/{classId}/enrollments")
    @Operation(
            summary = "Get class enrollments",
            description = "Retrieve paginated list of students enrolled in a class with optional status filter"
    )
    public ResponseEntity<ResponseObject<Page<EnrollmentDetailDTO>>> getClassEnrollments(
            @Parameter(description = "Class ID", required = true)
            @PathVariable Long classId,
            
            @Parameter(description = "Filter by enrollment status (enrolled, waitlisted, dropped, completed)")
            @RequestParam(required = false) String status,
            
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            
            @Parameter(description = "Sort by field")
            @RequestParam(defaultValue = "enrolledAt") String sortBy,
            
            @Parameter(description = "Sort direction (asc/desc)")
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.info("Getting enrollments for class ID: {} with status: {}", classId, status);

        Sort sort = sortDir.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<EnrollmentDetailDTO> enrollments = enrollmentService.getClassEnrollments(classId, status, pageable);

        return ResponseEntity.ok(
                new ResponseObject<>(HttpStatus.OK.value(), "Retrieved class enrollments successfully", enrollments));
    }

    /**
     * Remove a student from a class (unenroll/drop)
     * 
     * DELETE /api/v1/classes/{classId}/enrollments/{studentId}
     */
    @DeleteMapping("/classes/{classId}/enrollments/{studentId}")
    @Operation(
            summary = "Remove student from class",
            description = "Unenroll/drop a student from a class. " +
                    "Updates enrollment status to DROPPED and removes future StudentSession records."
    )
    public ResponseEntity<ResponseObject<Void>> removeStudentFromClass(
            @Parameter(description = "Class ID", required = true)
            @PathVariable Long classId,
            
            @Parameter(description = "Student ID to remove", required = true)
            @PathVariable Long studentId) {
        
        log.info("Removing student ID: {} from class ID: {}", studentId, classId);

        enrollmentService.removeStudentFromClass(classId, studentId);

        return ResponseEntity.ok(
                new ResponseObject<>(HttpStatus.OK.value(), "Student removed from class successfully", null));
    }

    /**
     * Get all enrollments for a specific student
     * 
     * GET /api/v1/students/{studentId}/enrollments
     */
    @GetMapping("/students/{studentId}/enrollments")
    @Operation(
            summary = "Get student enrollments",
            description = "Retrieve all classes a student is enrolled in with optional status filter"
    )
    public ResponseEntity<ResponseObject<List<EnrollmentDetailDTO>>> getStudentEnrollments(
            @Parameter(description = "Student ID", required = true)
            @PathVariable Long studentId,
            
            @Parameter(description = "Filter by enrollment statuses (comma-separated: enrolled,ongoing)")
            @RequestParam(required = false) List<String> status) {
        
        log.info("Getting enrollments for student ID: {} with statuses: {}", studentId, status);

        List<EnrollmentDetailDTO> enrollments = enrollmentService.getStudentEnrollments(studentId, status);

        return ResponseEntity.ok(
                new ResponseObject<>(HttpStatus.OK.value(), "Retrieved student enrollments successfully", enrollments));
    }

    /**
     * Get available students for enrollment in a class
     * Filters out students already enrolled
     * 
     * GET /api/v1/students/available-for-class/{classId}
     */
    @GetMapping("/students/available-for-class/{classId}")
    @Operation(
            summary = "Get available students for enrollment",
            description = "Retrieve paginated list of students available to enroll in a class. " +
                    "Filters out students already enrolled. Supports search by name, code, or email."
    )
    public ResponseEntity<ResponseObject<Page<AvailableStudentDTO>>> getAvailableStudentsForClass(
            @Parameter(description = "Class ID", required = true)
            @PathVariable Long classId,
            
            @Parameter(description = "Search term (name, code, email)")
            @RequestParam(required = false) String search,
            
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            
            @Parameter(description = "Sort by field")
            @RequestParam(defaultValue = "studentCode") String sortBy,
            
            @Parameter(description = "Sort direction (asc/desc)")
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        log.info("Getting available students for class ID: {} with search: {}", classId, search);

        Sort sort = sortDir.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<AvailableStudentDTO> students = enrollmentService.getAvailableStudentsForClass(
                classId, search, pageable);

        return ResponseEntity.ok(
                new ResponseObject<>(HttpStatus.OK.value(), "Retrieved available students successfully", students));
    }
}
