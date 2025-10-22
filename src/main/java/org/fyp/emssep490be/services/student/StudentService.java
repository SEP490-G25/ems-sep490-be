package org.fyp.emssep490be.services.student;

import org.fyp.emssep490be.dtos.student.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

public interface StudentService {
    // CRUD Operations
    StudentDTO createStudent(CreateStudentRequestDTO request);
    StudentDTO updateStudent(Long id, UpdateStudentRequestDTO request);
    BulkImportStudentResponseDTO bulkImportStudents(MultipartFile file, Long branchId);
    
    // Query Operations
    Page<StudentListDTO> getAllStudents(String search, Long branchId, Pageable pageable);
    StudentProfileDTO getStudentProfile(Long id);
    StudentScheduleDTO getStudentSchedule(Long id, LocalDate dateFrom, LocalDate dateTo, Long classId);
}
