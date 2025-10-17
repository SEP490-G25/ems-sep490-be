package org.fyp.emssep490be.dtos.level;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateLevelRequestDTO {

    private String name;

    private String standardType;

    @Positive(message = "Expected duration must be positive")
    private Integer expectedDurationHours;

    private Integer sortOrder;

    private String description;
}
