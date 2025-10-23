package org.fyp.emssep490be.services.teacher;

import org.fyp.emssep490be.dtos.teacher.CreateTeacherRequestDTO;
import org.fyp.emssep490be.dtos.teacher.TeacherProfileDTO;
import org.fyp.emssep490be.dtos.teacher.UpdateTeacherRequestDTO;

public interface TeacherService {
    TeacherProfileDTO getTeacherProfile(Long id);
    TeacherProfileDTO createTeacher(CreateTeacherRequestDTO request);
    TeacherProfileDTO updateTeacher(Long id, UpdateTeacherRequestDTO request);
    void deleteTeacher(Long id);
    // Object getTeacherSchedule(Long id, String dateFrom, String dateTo);
    // Object getTeacherWorkload(Long id, String dateFrom, String dateTo);
}
