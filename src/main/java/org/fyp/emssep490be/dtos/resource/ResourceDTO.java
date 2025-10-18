package org.fyp.emssep490be.dtos.resource;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResourceDTO {

    private Long id;

    private Long branchId;

    private String resourceType;

    private String name;

    // For ROOM type
    private String location;

    private Integer capacity;

    private String equipment;

    private String description;

    // For VIRTUAL type
    private String meetingUrl;

    private String meetingId;

    private String accountEmail;

    private String licenseType;

    private LocalDate expiryDate;

    private LocalDate renewalDate;

    private Long createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
