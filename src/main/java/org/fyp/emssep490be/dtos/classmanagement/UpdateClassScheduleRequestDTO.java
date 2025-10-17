package org.fyp.emssep490be.dtos.classmanagement;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateClassScheduleRequestDTO {

    @NotNull(message = "Effective from date is required")
    private LocalDate effectiveFrom;

    @NotNull(message = "Target day of week is required")
    private Integer targetDow;

    @NotNull(message = "New slot ID is required")
    private Long newSlotId;

    private String reason;
}
