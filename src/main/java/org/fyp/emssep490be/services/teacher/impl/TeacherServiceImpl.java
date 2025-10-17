package org.fyp.emssep490be.services.teacher.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fyp.emssep490be.dtos.teacher.TeacherProfileDTO;
import org.fyp.emssep490be.repositories.TeacherRepository;
import org.fyp.emssep490be.services.teacher.TeacherService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TeacherServiceImpl implements TeacherService {

    private final TeacherRepository teacherRepository;

    @Override
    @Transactional(readOnly = true)
    public TeacherProfileDTO getTeacherProfile(Long id) {
        log.info("Getting teacher profile for ID: {}", id);
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public Object getTeacherSchedule(Long id, String dateFrom, String dateTo) {
        log.info("Getting teacher schedule for ID: {}", id);
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public Object getTeacherWorkload(Long id) {
        log.info("Getting teacher workload for ID: {}", id);
        return null;
    }
}
