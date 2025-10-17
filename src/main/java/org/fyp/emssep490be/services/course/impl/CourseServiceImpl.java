package org.fyp.emssep490be.services.course.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fyp.emssep490be.dtos.common.PagedResponseDTO;
import org.fyp.emssep490be.dtos.course.*;
import org.fyp.emssep490be.repositories.CourseRepository;
import org.fyp.emssep490be.services.course.CourseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDTO<CourseDTO> getAllCourses(Long subjectId, Long levelId, String status, Boolean approved, Integer page, Integer limit) {
        log.info("Getting all courses");
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public CourseDetailDTO getCourseById(Long id) {
        log.info("Getting course by ID: {}", id);
        return null;
    }

    @Override
    public CourseDTO createCourse(CreateCourseRequestDTO request) {
        log.info("Creating course: {}", request.getCode());
        return null;
    }

    @Override
    public CourseDTO updateCourse(Long id, UpdateCourseRequestDTO request) {
        log.info("Updating course ID: {}", id);
        return null;
    }

    @Override
    public CourseDTO submitCourseForApproval(Long id) {
        log.info("Submitting course ID: {} for approval", id);
        return null;
    }

    @Override
    public CourseDTO approveCourse(Long id, ApprovalRequestDTO request) {
        log.info("Processing approval for course ID: {}, action: {}", id, request.getAction());
        return null;
    }

    @Override
    public void deleteCourse(Long id) {
        log.info("Deleting course ID: {}", id);
    }
}
