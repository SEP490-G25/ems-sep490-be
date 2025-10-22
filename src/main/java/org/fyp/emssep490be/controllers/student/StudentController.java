package org.fyp.emssep490be.controllers.student;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fyp.emssep490be.dtos.ResponseObject;
import org.fyp.emssep490be.dtos.student.*;
import org.fyp.emssep490be.services.student.StudentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

/**
 * Controller for Student operations
 * Base path: /api/v1/students
 */
@RestController
@RequestMapping("/api/v1/students")
@RequiredArgsConstructor
@Tag(name = "Student Management", description = "APIs for student CRUD operations and queries")
public class StudentController {

    private final StudentService studentService;

    /**
     * Get all students with pagination, search, and filter
     * GET /api/v1/students
     */
    @GetMapping
    @Operation(
            summary = "List all students",
            description = "Get paginated list of students with search and branch filter. " +
                    "Supports search by student code, name, or email."
    )
    public ResponseEntity<ResponseObject<Page<StudentListDTO>>> getAllStudents(
            @Parameter(description = "Search term (student code, name, email)")
            @RequestParam(required = false) String search,
            
            @Parameter(description = "Filter by branch ID")
            @RequestParam(required = false) Long branchId,
            
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            
            @Parameter(description = "Sort by field (studentCode, fullName, createdAt)")
            @RequestParam(defaultValue = "studentCode") String sortBy,
            
            @Parameter(description = "Sort direction (asc/desc)")
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<StudentListDTO> students = studentService.getAllStudents(search, branchId, pageable);

        return ResponseEntity.ok(
                new ResponseObject<>(HttpStatus.OK.value(), "Students retrieved successfully", students));
    }

    /**
     * Create a new student (ACADEMIC_STAFF)
     * POST /api/v1/students
     */
    @PostMapping
    @Operation(
            summary = "Create a new student",
            description = "Academic Staff creates a student with auto-generated student code and default password"
    )
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
     * Get student profile with enrollments
     * GET /api/v1/students/{id}
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Get student profile",
            description = "Retrieve detailed student profile including current enrollments and attendance summary"
    )
    public ResponseEntity<ResponseObject<StudentProfileDTO>> getStudentProfile(@PathVariable Long id) {
        StudentProfileDTO profile = studentService.getStudentProfile(id);
        return ResponseEntity.ok(
                new ResponseObject<>(HttpStatus.OK.value(), "Student profile retrieved", profile));
    }

    /**
     * Get student schedule with date range filter
     * GET /api/v1/students/{id}/schedule
     */
    @GetMapping("/{id}/schedule")
    @Operation(
            summary = "Get student schedule",
            description = "Retrieve student's class schedule with date range filter. " +
                    "Returns sessions with attendance status, class details, teacher, and resource information."
    )
    public ResponseEntity<ResponseObject<StudentScheduleDTO>> getStudentSchedule(
            @Parameter(description = "Student ID", required = true)
            @PathVariable Long id,
            
            @Parameter(description = "Start date (YYYY-MM-DD)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            
            @Parameter(description = "End date (YYYY-MM-DD)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            
            @Parameter(description = "Filter by specific class ID")
            @RequestParam(required = false) Long classId) {
        
        StudentScheduleDTO schedule = studentService.getStudentSchedule(id, dateFrom, dateTo, classId);
        return ResponseEntity.ok(
                new ResponseObject<>(HttpStatus.OK.value(), "Schedule retrieved successfully", schedule));
    }
}
