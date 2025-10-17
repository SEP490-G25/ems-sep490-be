package org.fyp.emssep490be.services.coursephase;

import org.fyp.emssep490be.dtos.coursephase.CoursePhaseDTO;
import org.fyp.emssep490be.dtos.coursephase.CreateCoursePhaseRequestDTO;

import java.util.List;

public interface CoursePhaseService {
    List<CoursePhaseDTO> getPhasesByCourse(Long courseId);
    CoursePhaseDTO createPhase(Long courseId, CreateCoursePhaseRequestDTO request);
    void deletePhase(Long courseId, Long id);
}
