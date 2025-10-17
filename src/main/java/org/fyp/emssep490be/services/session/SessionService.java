package org.fyp.emssep490be.services.session;

import org.fyp.emssep490be.dtos.common.PagedResponseDTO;
import org.fyp.emssep490be.dtos.session.*;

import java.time.LocalDate;
import java.util.List;

public interface SessionService {
    PagedResponseDTO<SessionDTO> getSessionsByClass(Long classId, LocalDate dateFrom, LocalDate dateTo,
                                                     String status, String type, Integer page, Integer limit);
    SessionDetailDTO getSessionById(Long id);
    SessionDTO updateSession(Long id, UpdateSessionRequestDTO request);
    CancelSessionResponseDTO cancelSession(Long id, CancelSessionRequestDTO request);
    AssignTeacherResponseDTO assignTeacher(Long sessionId, AssignTeacherRequestDTO request);
    List<AvailableTeacherDTO> getAvailableTeachers(Long sessionId, String skill);
    void removeTeacher(Long sessionId, Long teacherId, String skill);
    AssignResourceResponseDTO assignResource(Long sessionId, AssignResourceRequestDTO request);
    List<AvailableResourceDTO> getAvailableResources(Long sessionId, String resourceType);
    void removeResource(Long sessionId, Long resourceId);
}
