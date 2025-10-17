package org.fyp.emssep490be.dtos.session;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvailableTeacherDTO {

    private Long teacherId;

    private String teacherName;

    private String employeeCode;

    private List<TeacherSkill> skills;

    private AvailabilityInfo availability;

    private Integer currentSessionsOnDate;

    private Boolean isOtRegistered;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeacherSkill {
        private String skill;
        private Integer level;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AvailabilityInfo {
        private Integer dayOfWeek;
        private LocalTime startTime;
        private LocalTime endTime;
        private Boolean isAvailable;
    }
}
