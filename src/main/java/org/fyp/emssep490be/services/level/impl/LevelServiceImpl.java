package org.fyp.emssep490be.services.level.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fyp.emssep490be.dtos.level.CreateLevelRequestDTO;
import org.fyp.emssep490be.dtos.level.LevelDTO;
import org.fyp.emssep490be.dtos.level.UpdateLevelRequestDTO;
import org.fyp.emssep490be.repositories.LevelRepository;
import org.fyp.emssep490be.services.level.LevelService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LevelServiceImpl implements LevelService {

    private final LevelRepository levelRepository;

    @Override
    @Transactional(readOnly = true)
    public List<LevelDTO> getLevelsBySubject(Long subjectId) {
        log.info("Getting levels for subject ID: {}", subjectId);
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public LevelDTO getLevelById(Long subjectId, Long id) {
        log.info("Getting level ID: {} for subject ID: {}", id, subjectId);
        return null;
    }

    @Override
    public LevelDTO createLevel(Long subjectId, CreateLevelRequestDTO request) {
        log.info("Creating level for subject ID: {}", subjectId);
        return null;
    }

    @Override
    public LevelDTO updateLevel(Long subjectId, Long id, UpdateLevelRequestDTO request) {
        log.info("Updating level ID: {} for subject ID: {}", id, subjectId);
        return null;
    }

    @Override
    public void deleteLevel(Long subjectId, Long id) {
        log.info("Deleting level ID: {} for subject ID: {}", id, subjectId);
    }
}
