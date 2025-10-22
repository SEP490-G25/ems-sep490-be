package org.fyp.emssep490be.dtos.enrollment;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for batch enrolling students to a class
 * Academic Staff selects multiple students and adds them to a class
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchEnrollRequestDTO {

    @NotNull(message = "Class ID is required")
    private Long classId;

    @NotEmpty(message = "Student IDs list cannot be empty")
    private List<Long> studentIds;
}
