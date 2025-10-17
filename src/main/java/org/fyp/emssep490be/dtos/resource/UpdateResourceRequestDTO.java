package org.fyp.emssep490be.dtos.resource;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateResourceRequestDTO {

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
