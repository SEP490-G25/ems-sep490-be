package org.fyp.emssep490be.dtos.classmanagement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateClassResponseDTO {

    private Long id;

    private String code;

    private String status;

    private Integer sessionsGenerated;

    private Long createdBy;

    private LocalDateTime createdAt;

    private String message;
}
