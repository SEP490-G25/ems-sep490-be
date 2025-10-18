package org.fyp.emssep490be.dtos.timeslot;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeSlotDTO {

    private Long id;

    private Long branchId;

    private String name;

    private LocalTime startTime;

    private LocalTime endTime;

    private Integer durationMinutes;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
