package org.fyp.emssep490be.services.coursephase;

import org.fyp.emssep490be.dtos.coursephase.CoursePhaseDTO;
import org.fyp.emssep490be.dtos.coursephase.CreateCoursePhaseRequestDTO;
import org.fyp.emssep490be.dtos.coursephase.UpdateCoursePhaseRequestDTO;

import java.util.List;

public interface CoursePhaseService {
    List<CoursePhaseDTO> getPhasesByCourse(Long courseId);
    CoursePhaseDTO createPhase(Long courseId, CreateCoursePhaseRequestDTO request);
    CoursePhaseDTO updatePhase(Long phaseId, UpdateCoursePhaseRequestDTO request);
    void deletePhase(Long phaseId);
}
