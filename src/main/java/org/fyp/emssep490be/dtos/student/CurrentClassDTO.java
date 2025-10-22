package org.fyp.emssep490be.dtos.student;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for current class in student profile
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CurrentClassDTO {
    
    private Long classId;
    private String classCode;
    private String className;
    private String enrollmentStatus;
    private LocalDateTime enrolledAt;
    private AttendanceSummaryDTO attendanceSummary;
}
