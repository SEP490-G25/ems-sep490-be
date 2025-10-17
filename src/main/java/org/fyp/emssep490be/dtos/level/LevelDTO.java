package org.fyp.emssep490be.dtos.level;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LevelDTO {

    private Long id;

    private Long subjectId;

    private String code;

    private String name;

    private String standardType;

    private Integer expectedDurationHours;

    private Integer sortOrder;

    private String description;

    private LocalDateTime createdAt;
}
