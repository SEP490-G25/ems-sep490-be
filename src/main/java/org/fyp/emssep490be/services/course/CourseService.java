package org.fyp.emssep490be.services.course;

import org.fyp.emssep490be.dtos.common.PagedResponseDTO;
import org.fyp.emssep490be.dtos.course.*;

public interface CourseService {
    PagedResponseDTO<CourseDTO> getAllCourses(Long subjectId, Long levelId, String status, Boolean approved, Integer page, Integer limit);
    CourseDetailDTO getCourseById(Long id);
    CourseDTO createCourse(CreateCourseRequestDTO request);
    CourseDTO updateCourse(Long id, UpdateCourseRequestDTO request);
    CourseDTO submitCourseForApproval(Long id);
    CourseDTO approveCourse(Long id, ApprovalRequestDTO request);
    void deleteCourse(Long id);
}
