package org.fyp.emssep490be.dtos.classmanagement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateClassScheduleResponseDTO {

    private Long id;

    private String code;

    private Integer sessionsUpdated;

    private List<Object> conflictsDetected;

    private String message;
}
