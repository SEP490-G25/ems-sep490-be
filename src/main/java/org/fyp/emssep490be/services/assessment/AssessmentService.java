package org.fyp.emssep490be.services.assessment;

public interface AssessmentService {
    Object getAssessmentsByClass(Long classId);
    Object createAssessment(Long classId);
    Object recordScores(Long assessmentId);
}
