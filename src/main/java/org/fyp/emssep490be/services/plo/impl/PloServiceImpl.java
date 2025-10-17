package org.fyp.emssep490be.services.plo.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fyp.emssep490be.dtos.plo.CreatePloRequestDTO;
import org.fyp.emssep490be.dtos.plo.PloDTO;
import org.fyp.emssep490be.repositories.PloRepository;
import org.fyp.emssep490be.services.plo.PloService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PloServiceImpl implements PloService {

    private final PloRepository ploRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PloDTO> getPlosBySubject(Long subjectId) {
        log.info("Getting PLOs for subject ID: {}", subjectId);
        return null;
    }

    @Override
    public PloDTO createPlo(Long subjectId, CreatePloRequestDTO request) {
        log.info("Creating PLO for subject ID: {}", subjectId);
        return null;
    }

    @Override
    public void deletePlo(Long subjectId, Long id) {
        log.info("Deleting PLO ID: {} for subject ID: {}", id, subjectId);
    }
}
