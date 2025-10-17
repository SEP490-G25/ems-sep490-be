package org.fyp.emssep490be.dtos.course;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.fyp.emssep490be.dtos.clo.CloDTO;
import org.fyp.emssep490be.dtos.coursematerial.CourseMaterialDTO;
import org.fyp.emssep490be.dtos.coursephase.CoursePhaseDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseDetailDTO {

    private Long id;

    private Long subjectId;

    private Long levelId;

    private String code;

    private String name;

    private Integer version;

    private String description;

    private Double totalHours;

    private Integer durationWeeks;

    private Integer sessionPerWeek;

    private Double hoursPerSession;

    private String prerequisites;

    private String targetAudience;

    private String teachingMethods;

    private String status;

    private Long approvedByManager;

    private LocalDateTime approvedAt;

    private Long createdBy;

    private LocalDateTime createdAt;

    private LocalDate effectiveDate;

    private List<CoursePhaseDTO> phases;

    private List<CloDTO> clos;

    private List<CourseMaterialDTO> materials;
}
