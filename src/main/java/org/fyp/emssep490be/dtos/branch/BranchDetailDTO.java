package org.fyp.emssep490be.dtos.branch;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.fyp.emssep490be.dtos.resource.ResourceDTO;
import org.fyp.emssep490be.dtos.timeslot.TimeSlotDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BranchDetailDTO {

    private Long id;

    private Long centerId;

    private String code;

    private String name;

    private String address;

    private String location;

    private String phone;

    private Integer capacity;

    private String status;

    private LocalDate openingDate;

    private List<TimeSlotDTO> timeSlots;

    private List<ResourceDTO> resources;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
