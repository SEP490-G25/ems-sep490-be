package org.fyp.emssep490be.dtos.coursephase;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoursePhaseDTO {

    private Long id;

    private Long courseId;

    private Integer phaseNumber;

    private String name;

    private Integer durationWeeks;

    private String learningFocus;

    private Integer sortOrder;

    private Integer sessionsCount;

    private LocalDateTime createdAt;
}
