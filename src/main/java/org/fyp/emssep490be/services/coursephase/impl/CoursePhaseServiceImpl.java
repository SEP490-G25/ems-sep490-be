package org.fyp.emssep490be.services.coursephase.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fyp.emssep490be.dtos.coursephase.CoursePhaseDTO;
import org.fyp.emssep490be.dtos.coursephase.CreateCoursePhaseRequestDTO;
import org.fyp.emssep490be.repositories.CoursePhaseRepository;
import org.fyp.emssep490be.services.coursephase.CoursePhaseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CoursePhaseServiceImpl implements CoursePhaseService {

    private final CoursePhaseRepository coursePhaseRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CoursePhaseDTO> getPhasesByCourse(Long courseId) {
        log.info("Getting phases for course ID: {}", courseId);
        return null;
    }

    @Override
    public CoursePhaseDTO createPhase(Long courseId, CreateCoursePhaseRequestDTO request) {
        log.info("Creating phase for course ID: {}", courseId);
        return null;
    }

    @Override
    public void deletePhase(Long courseId, Long id) {
        log.info("Deleting phase ID: {} for course ID: {}", id, courseId);
    }
}
