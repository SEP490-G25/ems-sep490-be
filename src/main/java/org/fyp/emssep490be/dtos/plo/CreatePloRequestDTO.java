package org.fyp.emssep490be.dtos.plo;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePloRequestDTO {

    @NotBlank(message = "PLO code is required")
    private String code;

    @NotBlank(message = "PLO description is required")
    private String description;
}
