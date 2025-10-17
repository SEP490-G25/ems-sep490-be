package org.fyp.emssep490be.dtos.branch;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBranchRequestDTO {

    private String name;

    private String address;

    private String location;

    private String phone;

    @Positive(message = "Capacity must be positive")
    private Integer capacity;

    private String status;

    private LocalDate openingDate;
}
