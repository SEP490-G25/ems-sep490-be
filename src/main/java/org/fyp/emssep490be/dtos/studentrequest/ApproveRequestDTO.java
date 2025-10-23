package org.fyp.emssep490be.dtos.studentrequest;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for approving a student request
 * Used by Academic Staff to approve absence/makeup/transfer requests
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApproveRequestDTO {

    /**
     * Optional notes from the staff when approving the request
     */
    @Size(max = 500, message = "Decision notes must not exceed 500 characters")
    private String decisionNotes;
}
