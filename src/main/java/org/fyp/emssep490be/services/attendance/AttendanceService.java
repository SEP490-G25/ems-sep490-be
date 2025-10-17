package org.fyp.emssep490be.services.attendance;

import org.fyp.emssep490be.dtos.attendance.RecordAttendanceRequestDTO;

public interface AttendanceService {
    Object recordAttendance(Long sessionId, RecordAttendanceRequestDTO request);
    Object getAttendance(Long sessionId);
}
