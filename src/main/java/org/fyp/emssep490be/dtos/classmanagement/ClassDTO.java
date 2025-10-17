package org.fyp.emssep490be.dtos.classmanagement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassDTO {

    private Long id;

    private Long branchId;

    private Long courseId;

    private String code;

    private String name;

    private String modality;

    private LocalDate startDate;

    private LocalDate plannedEndDate;

    private List<Integer> scheduleDays;

    private Integer maxCapacity;

    private Integer currentEnrollment;

    private String status;

    private Long createdBy;

    private Long approvedBy;

    private LocalDateTime approvedAt;

    private LocalDateTime createdAt;
}
