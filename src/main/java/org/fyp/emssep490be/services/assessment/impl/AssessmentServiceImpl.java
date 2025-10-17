package org.fyp.emssep490be.services.assessment.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fyp.emssep490be.repositories.AssessmentRepository;
import org.fyp.emssep490be.services.assessment.AssessmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AssessmentServiceImpl implements AssessmentService {

    private final AssessmentRepository assessmentRepository;

    @Override
    @Transactional(readOnly = true)
    public Object getAssessmentsByClass(Long classId) {
        log.info("Getting assessments for class ID: {}", classId);
        return null;
    }

    @Override
    public Object createAssessment(Long classId) {
        log.info("Creating assessment for class ID: {}", classId);
        return null;
    }

    @Override
    public Object recordScores(Long assessmentId) {
        log.info("Recording scores for assessment ID: {}", assessmentId);
        return null;
    }
}
