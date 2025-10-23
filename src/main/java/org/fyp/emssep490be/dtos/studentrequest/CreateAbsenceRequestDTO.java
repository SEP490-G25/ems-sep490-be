package org.fyp.emssep490be.dtos.studentrequest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating an absence request (Xin phép nghỉ)
 * Student submits this when they know they will be absent for a session
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAbsenceRequestDTO {

    /**
     * The session ID that the student wants to be absent from
     */
    @NotNull(message = "Target session ID is required")
    private Long targetSessionId;

    /**
     * Reason for absence
     * Must be at least 10 characters to ensure meaningful explanation
     */
    @NotBlank(message = "Reason is required")
    @Size(min = 10, max = 500, message = "Reason must be between 10 and 500 characters")
    private String reason;
}
