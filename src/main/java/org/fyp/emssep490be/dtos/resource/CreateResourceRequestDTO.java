package org.fyp.emssep490be.dtos.resource;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateResourceRequestDTO {

    @NotNull(message = "Resource type is required")
    private String resourceType;

    @NotBlank(message = "Resource name is required")
    private String name;

    // For ROOM type
    private String location;

    private Integer capacity;

    private String equipment;

    // For VIRTUAL type
    private String meetingUrl;

    private String meetingId;

    private String accountEmail;

    private String licenseType;

    private LocalDate expiryDate;

    private String description;
}
