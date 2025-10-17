package org.fyp.emssep490be.dtos.course;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCourseRequestDTO {

    private String name;

    private String description;

    @Positive(message = "Total hours must be positive")
    private Double totalHours;

    @Positive(message = "Duration weeks must be positive")
    private Integer durationWeeks;

    @Positive(message = "Session per week must be positive")
    private Integer sessionPerWeek;

    @Positive(message = "Hours per session must be positive")
    private Double hoursPerSession;

    private String prerequisites;

    private String targetAudience;

    private String teachingMethods;

    private LocalDate effectiveDate;

    private String status;
}
