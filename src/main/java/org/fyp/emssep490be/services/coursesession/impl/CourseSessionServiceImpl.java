package org.fyp.emssep490be.services.coursesession.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fyp.emssep490be.dtos.coursesession.CourseSessionDTO;
import org.fyp.emssep490be.dtos.coursesession.CreateCourseSessionRequestDTO;
import org.fyp.emssep490be.repositories.CourseSessionRepository;
import org.fyp.emssep490be.services.coursesession.CourseSessionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CourseSessionServiceImpl implements CourseSessionService {

    private final CourseSessionRepository courseSessionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CourseSessionDTO> getSessionsByPhase(Long phaseId) {
        log.info("Getting sessions for phase ID: {}", phaseId);
        return null;
    }

    @Override
    public CourseSessionDTO createSession(Long phaseId, CreateCourseSessionRequestDTO request) {
        log.info("Creating session for phase ID: {}", phaseId);
        return null;
    }

    @Override
    public void deleteSession(Long phaseId, Long id) {
        log.info("Deleting session ID: {} for phase ID: {}", id, phaseId);
    }
}
