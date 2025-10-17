package org.fyp.emssep490be.services.coursesession;

import org.fyp.emssep490be.dtos.coursesession.CourseSessionDTO;
import org.fyp.emssep490be.dtos.coursesession.CreateCourseSessionRequestDTO;

import java.util.List;

public interface CourseSessionService {
    List<CourseSessionDTO> getSessionsByPhase(Long phaseId);
    CourseSessionDTO createSession(Long phaseId, CreateCourseSessionRequestDTO request);
    void deleteSession(Long phaseId, Long id);
}
