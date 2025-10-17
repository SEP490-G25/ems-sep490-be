package org.fyp.emssep490be.services.clo.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fyp.emssep490be.dtos.clo.CloDTO;
import org.fyp.emssep490be.dtos.clo.CreateCloRequestDTO;
import org.fyp.emssep490be.dtos.clo.MappingRequestDTO;
import org.fyp.emssep490be.repositories.CloRepository;
import org.fyp.emssep490be.services.clo.CloService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CloServiceImpl implements CloService {

    private final CloRepository cloRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CloDTO> getClosByCourse(Long courseId) {
        log.info("Getting CLOs for course ID: {}", courseId);
        return null;
    }

    @Override
    public CloDTO createClo(Long courseId, CreateCloRequestDTO request) {
        log.info("Creating CLO for course ID: {}", courseId);
        return null;
    }

    @Override
    public Map<String, Object> mapPloToClo(Long ploId, Long cloId, MappingRequestDTO request) {
        log.info("Mapping PLO ID: {} to CLO ID: {}", ploId, cloId);
        return null;
    }

    @Override
    public Map<String, Object> mapCloToSession(Long sessionId, Long cloId, MappingRequestDTO request) {
        log.info("Mapping CLO ID: {} to Session ID: {}", cloId, sessionId);
        return null;
    }

    @Override
    public void deleteClo(Long courseId, Long id) {
        log.info("Deleting CLO ID: {} for course ID: {}", id, courseId);
    }
}
