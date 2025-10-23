package org.fyp.emssep490be.dtos.studentrequest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for rejecting a student request
 * Used by Academic Staff to reject absence/makeup/transfer requests
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RejectRequestDTO {

    /**
     * Required reason for rejection
     * Must explain why the request was rejected
     */
    @NotBlank(message = "Rejection reason is required")
    @Size(min = 10, max = 500, message = "Rejection reason must be between 10 and 500 characters")
    private String rejectionReason;
}
