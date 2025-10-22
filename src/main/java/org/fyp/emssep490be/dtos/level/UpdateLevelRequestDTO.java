package org.fyp.emssep490be.dtos.level;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating an existing level
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateLevelRequestDTO {

    private String name;

    private String standardType;

    @Positive(message = "Expected duration must be positive")
    private Integer expectedDurationHours;

    @Min(value = 0, message = "Sort order must be non-negative")
    private Integer sortOrder;

    private String description;
}
