package org.fyp.emssep490be.dtos.student;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO for student schedule summary statistics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentScheduleSummaryDTO {
    
    private Integer totalSessions;
    private Map<String, Integer> byStatus; // planned: 10, present: 5, absent: 2, etc.
}
