package org.fyp.emssep490be.services.student;

import org.fyp.emssep490be.dtos.enrollment.CreateEnrollmentRequestDTO;
import org.fyp.emssep490be.dtos.enrollment.EnrollmentDTO;
import org.fyp.emssep490be.dtos.student.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface StudentService {
    // CRUD Operations
    StudentDTO createStudent(CreateStudentRequestDTO request);
    StudentDTO updateStudent(Long id, UpdateStudentRequestDTO request);
    BulkImportStudentResponseDTO bulkImportStudents(MultipartFile file, Long branchId);
    
    // Query Operations
    StudentProfileDTO getStudentProfile(Long id);
    List<EnrollmentDTO> getStudentEnrollments(Long id);
    Object getStudentSchedule(Long id);
    
    // Enrollment
    EnrollmentDTO enrollStudent(Long id, CreateEnrollmentRequestDTO request);
}
