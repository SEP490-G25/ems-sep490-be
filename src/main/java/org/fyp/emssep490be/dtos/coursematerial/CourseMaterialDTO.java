package org.fyp.emssep490be.dtos.coursematerial;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseMaterialDTO {

    private Long id;

    private Long courseId;

    private Long phaseId;

    private Long courseSessionId;

    private String title;

    private String url;

    private Long uploadedBy;

    private LocalDateTime createdAt;
}
