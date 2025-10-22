package org.fyp.emssep490be.dtos.coursephase;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCoursePhaseRequestDTO {

    @NotBlank(message = "Phase name is required")
    private String name;

    @Positive(message = "Duration weeks must be positive")
    private Integer durationWeeks;

    private String learningFocus;

    private Integer sortOrder;
}
