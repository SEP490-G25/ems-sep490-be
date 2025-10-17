package org.fyp.emssep490be.dtos.timeslot;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTimeSlotRequestDTO {

    private String name;

    private LocalTime startTime;

    private LocalTime endTime;

    @Positive(message = "Duration must be positive")
    private Integer durationMin;
}
