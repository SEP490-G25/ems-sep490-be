package org.fyp.emssep490be.services.plo;

import org.fyp.emssep490be.dtos.plo.CreatePloRequestDTO;
import org.fyp.emssep490be.dtos.plo.PloDTO;

import java.util.List;

public interface PloService {
    List<PloDTO> getPlosBySubject(Long subjectId);
    PloDTO createPlo(Long subjectId, CreatePloRequestDTO request);
    void deletePlo(Long subjectId, Long id);
}
