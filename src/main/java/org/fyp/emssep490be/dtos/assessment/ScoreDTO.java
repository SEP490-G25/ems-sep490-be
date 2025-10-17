package org.fyp.emssep490be.dtos.assessment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScoreDTO {
    private Long id;
    private Long assessmentId;
    private Long studentId;
    private String studentName;
    private Double score;
    private String feedback;
}
