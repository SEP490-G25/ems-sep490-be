package org.fyp.emssep490be.dtos.attendance;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecordAttendanceRequestDTO {
    @NotNull
    private List<AttendanceRecord> attendanceRecords;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttendanceRecord {
        private Long studentSessionId;
        private String attendanceStatus;
        private String note;
    }
}
