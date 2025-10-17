package org.fyp.emssep490be.dtos.course;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalRequestDTO {

    @NotBlank(message = "Action is required (approve or reject)")
    private String action;

    private String rejectionReason;
}
