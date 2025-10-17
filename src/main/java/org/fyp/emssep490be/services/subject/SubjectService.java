package org.fyp.emssep490be.services.subject;

import org.fyp.emssep490be.dtos.common.PagedResponseDTO;
import org.fyp.emssep490be.dtos.subject.CreateSubjectRequestDTO;
import org.fyp.emssep490be.dtos.subject.SubjectDTO;
import org.fyp.emssep490be.dtos.subject.UpdateSubjectRequestDTO;

public interface SubjectService {
    PagedResponseDTO<SubjectDTO> getAllSubjects(String status, Integer page, Integer limit);
    SubjectDTO getSubjectById(Long id);
    SubjectDTO createSubject(CreateSubjectRequestDTO request);
    SubjectDTO updateSubject(Long id, UpdateSubjectRequestDTO request);
    void deleteSubject(Long id);
}
