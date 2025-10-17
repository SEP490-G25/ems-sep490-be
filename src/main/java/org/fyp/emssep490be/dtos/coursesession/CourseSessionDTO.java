package org.fyp.emssep490be.dtos.coursesession;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.fyp.emssep490be.dtos.clo.CloDTO;
import org.fyp.emssep490be.dtos.coursematerial.CourseMaterialDTO;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseSessionDTO {

    private Long id;

    private Long phaseId;

    private Integer sequenceNo;

    private String topic;

    private String studentTask;

    private List<String> skillSet;

    private List<CloDTO> clos;

    private List<CourseMaterialDTO> materials;

    private LocalDateTime createdAt;
}
