package org.fyp.emssep490be.dtos.clo;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCloRequestDTO {

    @NotBlank(message = "CLO code is required")
    private String code;

    @NotBlank(message = "CLO description is required")
    private String description;
}
