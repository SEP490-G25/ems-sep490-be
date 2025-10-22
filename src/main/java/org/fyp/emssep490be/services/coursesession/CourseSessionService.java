package org.fyp.emssep490be.services.coursesession;

import org.fyp.emssep490be.dtos.coursesession.CourseSessionDTO;
import org.fyp.emssep490be.dtos.coursesession.CreateCourseSessionRequestDTO;
import org.fyp.emssep490be.dtos.coursesession.UpdateCourseSessionRequestDTO;

import java.util.List;

public interface CourseSessionService {
    List<CourseSessionDTO> getSessionsByPhase(Long phaseId);
    CourseSessionDTO createSession(Long phaseId, CreateCourseSessionRequestDTO request);
    CourseSessionDTO updateSession(Long sessionId, UpdateCourseSessionRequestDTO request);
    void deleteSession(Long sessionId);
}
