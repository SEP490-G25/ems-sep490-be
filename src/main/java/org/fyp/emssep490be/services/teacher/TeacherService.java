package org.fyp.emssep490be.services.teacher;

import org.fyp.emssep490be.dtos.teacher.CreateTeacherRequest;
import org.fyp.emssep490be.dtos.teacher.TeacherProfileDTO;

public interface TeacherService {
    TeacherProfileDTO getTeacherProfile(Long id);
    TeacherProfileDTO createTeacher(CreateTeacherRequest request);
    // Object getTeacherSchedule(Long id, String dateFrom, String dateTo);
    // Object getTeacherWorkload(Long id, String dateFrom, String dateTo);
}
