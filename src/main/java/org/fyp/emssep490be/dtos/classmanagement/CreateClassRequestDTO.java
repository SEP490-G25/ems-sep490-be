package org.fyp.emssep490be.dtos.classmanagement;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateClassRequestDTO {

    @NotNull(message = "Branch ID is required")
    private Long branchId;

    @NotNull(message = "Course ID is required")
    private Long courseId;

    @NotBlank(message = "Class code is required")
    private String code;

    @NotBlank(message = "Class name is required")
    private String name;

    @NotNull(message = "Modality is required")
    private String modality;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "Schedule days are required")
    private List<Integer> scheduleDays;

    @NotNull(message = "Schedule mapping is required")
    private Map<String, ScheduleSlotMapping> scheduleMapping;

    @Positive(message = "Max capacity must be positive")
    private Integer maxCapacity;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScheduleSlotMapping {
        private Long slotId;
    }
}
