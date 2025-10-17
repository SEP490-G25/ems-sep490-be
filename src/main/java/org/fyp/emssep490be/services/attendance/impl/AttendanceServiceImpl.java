package org.fyp.emssep490be.services.attendance.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fyp.emssep490be.dtos.attendance.RecordAttendanceRequestDTO;
import org.fyp.emssep490be.repositories.StudentSessionRepository;
import org.fyp.emssep490be.services.attendance.AttendanceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AttendanceServiceImpl implements AttendanceService {

    private final StudentSessionRepository studentSessionRepository;

    @Override
    public Object recordAttendance(Long sessionId, RecordAttendanceRequestDTO request) {
        log.info("Recording attendance for session ID: {}", sessionId);
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public Object getAttendance(Long sessionId) {
        log.info("Getting attendance for session ID: {}", sessionId);
        return null;
    }
}
