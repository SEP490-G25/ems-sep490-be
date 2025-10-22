package org.fyp.emssep490be.dtos.student;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO for complete student schedule response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentScheduleDTO {
    
    private Long studentId;
    private String studentName;
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private List<StudentScheduleSessionDTO> sessions;
    private StudentScheduleSummaryDTO summary;
}
