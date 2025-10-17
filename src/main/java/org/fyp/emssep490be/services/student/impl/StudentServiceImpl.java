package org.fyp.emssep490be.services.student.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fyp.emssep490be.dtos.enrollment.CreateEnrollmentRequestDTO;
import org.fyp.emssep490be.dtos.enrollment.EnrollmentDTO;
import org.fyp.emssep490be.dtos.student.StudentProfileDTO;
import org.fyp.emssep490be.repositories.StudentRepository;
import org.fyp.emssep490be.services.student.StudentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;

    @Override
    @Transactional(readOnly = true)
    public StudentProfileDTO getStudentProfile(Long id) {
        log.info("Getting student profile for ID: {}", id);
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentDTO> getStudentEnrollments(Long id) {
        log.info("Getting enrollments for student ID: {}", id);
        return null;
    }

    @Override
    public EnrollmentDTO enrollStudent(Long id, CreateEnrollmentRequestDTO request) {
        // TODO: Create Enrollment and auto-generate StudentSession records for all future sessions
        log.info("Enrolling student ID: {} in class ID: {}", id, request.getClassId());
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public Object getStudentSchedule(Long id) {
        log.info("Getting schedule for student ID: {}", id);
        return null;
    }
}
