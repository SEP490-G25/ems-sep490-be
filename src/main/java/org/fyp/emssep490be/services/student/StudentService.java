package org.fyp.emssep490be.services.student;

import org.fyp.emssep490be.dtos.enrollment.CreateEnrollmentRequestDTO;
import org.fyp.emssep490be.dtos.enrollment.EnrollmentDTO;
import org.fyp.emssep490be.dtos.student.StudentProfileDTO;

import java.util.List;

public interface StudentService {
    StudentProfileDTO getStudentProfile(Long id);
    List<EnrollmentDTO> getStudentEnrollments(Long id);
    EnrollmentDTO enrollStudent(Long id, CreateEnrollmentRequestDTO request);
    Object getStudentSchedule(Long id);
}
