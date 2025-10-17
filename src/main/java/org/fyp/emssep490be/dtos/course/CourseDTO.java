package org.fyp.emssep490be.dtos.course;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseDTO {

    private Long id;

    private Long subjectId;

    private Long levelId;

    private String code;

    private String name;

    private Integer version;

    private String description;

    private Double totalHours;

    private Integer durationWeeks;

    private Integer sessionPerWeek;

    private Double hoursPerSession;

    private String status;

    private Long approvedByManager;

    private LocalDateTime approvedAt;

    private Long createdBy;

    private LocalDateTime createdAt;

    private Integer phasesCount;

    private Integer sessionsCount;
}
