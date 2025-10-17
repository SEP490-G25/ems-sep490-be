package org.fyp.emssep490be.dtos.assessment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentDTO {
    private Long id;
    private Long classId;
    private String assessmentType;
    private String title;
    private LocalDate scheduledDate;
    private Double weight;
    private String status;
}
