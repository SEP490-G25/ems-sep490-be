package org.fyp.emssep490be.services.subject.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fyp.emssep490be.dtos.common.PagedResponseDTO;
import org.fyp.emssep490be.dtos.subject.CreateSubjectRequestDTO;
import org.fyp.emssep490be.dtos.subject.SubjectDTO;
import org.fyp.emssep490be.dtos.subject.UpdateSubjectRequestDTO;
import org.fyp.emssep490be.repositories.SubjectRepository;
import org.fyp.emssep490be.services.subject.SubjectService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SubjectServiceImpl implements SubjectService {

    private final SubjectRepository subjectRepository;

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDTO<SubjectDTO> getAllSubjects(String status, Integer page, Integer limit) {
        // TODO: Implement
        log.info("Getting all subjects");
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public SubjectDTO getSubjectById(Long id) {
        // TODO: Implement
        log.info("Getting subject by ID: {}", id);
        return null;
    }

    @Override
    public SubjectDTO createSubject(CreateSubjectRequestDTO request) {
        // TODO: Implement
        log.info("Creating subject: {}", request.getCode());
        return null;
    }

    @Override
    public SubjectDTO updateSubject(Long id, UpdateSubjectRequestDTO request) {
        // TODO: Implement
        log.info("Updating subject ID: {}", id);
        return null;
    }

    @Override
    public void deleteSubject(Long id) {
        // TODO: Implement
        log.info("Deleting subject ID: {}", id);
    }
}
