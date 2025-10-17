package org.fyp.emssep490be.dtos.session;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CancelSessionRequestDTO {

    @NotBlank(message = "Cancellation reason is required")
    private String reason;
}
