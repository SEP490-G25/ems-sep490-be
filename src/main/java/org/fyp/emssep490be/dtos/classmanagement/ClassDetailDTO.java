package org.fyp.emssep490be.dtos.classmanagement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassDetailDTO {

    private Long id;

    private BranchInfo branch;

    private CourseInfo course;

    private String code;

    private String name;

    private String modality;

    private LocalDate startDate;

    private LocalDate plannedEndDate;

    private LocalDate actualEndDate;

    private List<Integer> scheduleDays;

    private Map<String, ScheduleSlotInfo> scheduleMapping;

    private Integer maxCapacity;

    private Integer currentEnrollment;

    private String status;

    private Long createdBy;

    private LocalDateTime submittedAt;

    private Long approvedBy;

    private LocalDateTime approvedAt;

    private String rejectionReason;

    private LocalDateTime createdAt;

    private Integer sessionsCount;

    private Integer sessionsCompleted;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BranchInfo {
        private Long id;
        private String code;
        private String name;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseInfo {
        private Long id;
        private String code;
        private String name;
        private Double totalHours;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScheduleSlotInfo {
        private String startTime;
        private String endTime;
        private Long slotId;
    }
}
