package org.fyp.emssep490be.dtos.branch;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BranchDTO {

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

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
