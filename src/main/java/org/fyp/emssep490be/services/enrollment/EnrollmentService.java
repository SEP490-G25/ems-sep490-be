package org.fyp.emssep490be.services.enrollment;

import org.fyp.emssep490be.dtos.enrollment.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for managing student enrollments
 * Handles enrollment operations following session-first design pattern
 */
public interface EnrollmentService {

    /**
     * Batch enroll multiple students to a class
     * Supports partial success - returns detailed success/failure information
     * 
     * @param classId The class to enroll students into
     * @param studentIds List of student IDs to enroll
     * @return BatchEnrollResponseDTO with success and failure details
     */
    BatchEnrollResponseDTO batchEnrollStudents(Long classId, List<Long> studentIds);

    /**
     * Get all enrollments for a specific class with pagination
     * 
     * @param classId The class ID
     * @param status Optional enrollment status filter (enrolled, waitlisted, dropped, completed)
     * @param pageable Pagination parameters
     * @return Page of EnrollmentDetailDTO
     */
    Page<EnrollmentDetailDTO> getClassEnrollments(Long classId, String status, Pageable pageable);

    /**
     * Remove a student from a class (unenroll/drop)
     * Updates enrollment status to 'dropped' and removes future StudentSession records
     * 
     * @param classId The class ID
     * @param studentId The student ID
     */
    void removeStudentFromClass(Long classId, Long studentId);

    /**
     * Get all enrollments for a specific student
     * 
     * @param studentId The student ID
     * @param statusList Optional list of enrollment statuses to filter
     * @return List of EnrollmentDetailDTO
     */
    List<EnrollmentDetailDTO> getStudentEnrollments(Long studentId, List<String> statusList);

    /**
     * Get available students for enrollment in a class
     * Filters out students already enrolled in the class
     * 
     * @param classId The class ID
     * @param search Optional search term (name, code, email)
     * @param pageable Pagination parameters
     * @return Page of AvailableStudentDTO
     */
    Page<AvailableStudentDTO> getAvailableStudentsForClass(Long classId, String search, Pageable pageable);
}
