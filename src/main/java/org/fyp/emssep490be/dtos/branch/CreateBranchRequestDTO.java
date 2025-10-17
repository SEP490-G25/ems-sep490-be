package org.fyp.emssep490be.dtos.branch;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBranchRequestDTO {

    @NotNull(message = "Center ID is required")
    private Long centerId;

    @NotBlank(message = "Branch code is required")
    private String code;

    @NotBlank(message = "Branch name is required")
    private String name;

    @NotBlank(message = "Address is required")
    private String address;

    private String location;

    private String phone;

    @Positive(message = "Capacity must be positive")
    private Integer capacity;

    private String status;

    private LocalDate openingDate;
}
