package org.fyp.emssep490be.services.level;

import org.fyp.emssep490be.dtos.level.CreateLevelRequestDTO;
import org.fyp.emssep490be.dtos.level.LevelDTO;
import org.fyp.emssep490be.dtos.level.UpdateLevelRequestDTO;

import java.util.List;

public interface LevelService {
    List<LevelDTO> getLevelsBySubject(Long subjectId);
    LevelDTO getLevelById(Long subjectId, Long id);
    LevelDTO createLevel(Long subjectId, CreateLevelRequestDTO request);
    LevelDTO updateLevel(Long subjectId, Long id, UpdateLevelRequestDTO request);
    void deleteLevel(Long subjectId, Long id);
}
