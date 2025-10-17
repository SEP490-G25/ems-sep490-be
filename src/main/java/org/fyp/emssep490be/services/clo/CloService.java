package org.fyp.emssep490be.services.clo;

import org.fyp.emssep490be.dtos.clo.CloDTO;
import org.fyp.emssep490be.dtos.clo.CreateCloRequestDTO;
import org.fyp.emssep490be.dtos.clo.MappingRequestDTO;

import java.util.List;
import java.util.Map;

public interface CloService {
    List<CloDTO> getClosByCourse(Long courseId);
    CloDTO createClo(Long courseId, CreateCloRequestDTO request);
    Map<String, Object> mapPloToClo(Long ploId, Long cloId, MappingRequestDTO request);
    Map<String, Object> mapCloToSession(Long sessionId, Long cloId, MappingRequestDTO request);
    void deleteClo(Long courseId, Long id);
}
