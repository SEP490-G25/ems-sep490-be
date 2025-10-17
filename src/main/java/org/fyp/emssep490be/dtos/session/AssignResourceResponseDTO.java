package org.fyp.emssep490be.dtos.session;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignResourceResponseDTO {

    private Long sessionId;

    private String resourceType;

    private Long resourceId;

    private String resourceName;

    private Integer capacity;

    private Integer capacityOverride;

    private LocalDateTime createdAt;
}
