package org.fyp.emssep490be.dtos.subject;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new subject
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSubjectRequestDTO {

    @NotBlank(message = "Subject code is required")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "Subject code must contain only uppercase letters, numbers, and hyphens")
    private String code;

    @NotBlank(message = "Subject name is required")
    private String name;

    private String description;

    @NotNull(message = "Subject status is required")
    private String status;
}
