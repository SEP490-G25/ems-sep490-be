package org.fyp.emssep490be.services.session.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fyp.emssep490be.dtos.common.PagedResponseDTO;
import org.fyp.emssep490be.dtos.session.*;
import org.fyp.emssep490be.repositories.SessionRepository;
import org.fyp.emssep490be.services.session.SessionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SessionServiceImpl implements SessionService {

    private final SessionRepository sessionRepository;

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDTO<SessionDTO> getSessionsByClass(Long classId, LocalDate dateFrom, LocalDate dateTo,
                                                            String status, String type, Integer page, Integer limit) {
        log.info("Getting sessions for class ID: {}", classId);
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public SessionDetailDTO getSessionById(Long id) {
        log.info("Getting session by ID: {}", id);
        return null;
    }

    @Override
    public SessionDTO updateSession(Long id, UpdateSessionRequestDTO request) {
        log.info("Updating session ID: {}", id);
        return null;
    }

    @Override
    public CancelSessionResponseDTO cancelSession(Long id, CancelSessionRequestDTO request) {
        // TODO: Update session status, notify students
        log.info("Cancelling session ID: {}", id);
        return null;
    }

    @Override
    public AssignTeacherResponseDTO assignTeacher(Long sessionId, AssignTeacherRequestDTO request) {
        // TODO: Create TeachingSlot record
        log.info("Assigning teacher ID: {} to session ID: {}", request.getTeacherId(), sessionId);
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvailableTeacherDTO> getAvailableTeachers(Long sessionId, String skill) {
        // TODO: Query teachers with skill match, availability, and no conflict
        log.info("Getting available teachers for session ID: {}", sessionId);
        return null;
    }

    @Override
    public void removeTeacher(Long sessionId, Long teacherId, String skill) {
        // TODO: Delete TeachingSlot record
        log.info("Removing teacher ID: {} from session ID: {}", teacherId, sessionId);
    }

    @Override
    public AssignResourceResponseDTO assignResource(Long sessionId, AssignResourceRequestDTO request) {
        // TODO: Create SessionResource record
        log.info("Assigning resource ID: {} to session ID: {}", request.getResourceId(), sessionId);
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvailableResourceDTO> getAvailableResources(Long sessionId, String resourceType) {
        // TODO: Query resources with availability check
        log.info("Getting available resources for session ID: {}", sessionId);
        return null;
    }

    @Override
    public void removeResource(Long sessionId, Long resourceId) {
        // TODO: Delete SessionResource record
        log.info("Removing resource ID: {} from session ID: {}", resourceId, sessionId);
    }
}
