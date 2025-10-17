package org.fyp.emssep490be.dtos.session;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignResourceRequestDTO {

    @NotNull(message = "Resource type is required")
    private String resourceType;

    @NotNull(message = "Resource ID is required")
    private Long resourceId;

    private Integer capacityOverride;
}
