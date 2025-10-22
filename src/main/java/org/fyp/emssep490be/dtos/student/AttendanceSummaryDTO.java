package org.fyp.emssep490be.dtos.student;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for attendance summary in student profile
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceSummaryDTO {
    
    private Integer totalSessions;
    private Integer attended;
    private Integer absent;
    private Integer excused;
    private Double rate; // Attendance rate (attended / total)
}
