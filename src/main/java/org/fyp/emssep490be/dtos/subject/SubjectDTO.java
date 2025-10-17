package org.fyp.emssep490be.dtos.subject;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubjectDTO {

    private Long id;

    private String code;

    private String name;

    private String description;

    private String status;

    private Long createdBy;

    private LocalDateTime createdAt;

    private Integer levelsCount;

    private Integer coursesCount;
}
