package org.fyp.emssep490be.dtos.level;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new level under a subject
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateLevelRequestDTO {

    @NotBlank(message = "Level code is required")
    private String code;

    @NotBlank(message = "Level name is required")
    private String name;

    private String standardType;

    @Positive(message = "Expected duration must be positive")
    private Integer expectedDurationHours;

    @NotNull(message = "Sort order is required")
    @Min(value = 0, message = "Sort order must be non-negative")
    private Integer sortOrder;

    private String description;
}
