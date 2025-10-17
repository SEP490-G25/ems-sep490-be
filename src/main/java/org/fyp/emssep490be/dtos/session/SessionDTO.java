package org.fyp.emssep490be.dtos.session;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionDTO {

    private Long id;

    private Long classId;

    private Long courseSessionId;

    private LocalDate date;

    private LocalTime startTime;

    private LocalTime endTime;

    private String type;

    private String status;

    private CourseSessionInfo courseSession;

    private List<TeacherInfo> teachers;

    private List<ResourceInfo> resources;

    private AttendanceSummary attendanceSummary;

    private String teacherNote;

    private LocalDateTime createdAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseSessionInfo {
        private Integer sequenceNo;
        private String topic;
        private List<String> skillSet;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeacherInfo {
        private Long teacherId;
        private String teacherName;
        private String skill;
        private String role;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResourceInfo {
        private Long resourceId;
        private String resourceType;
        private String resourceName;
        private Integer capacity;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttendanceSummary {
        private Integer totalStudents;
        private Integer present;
        private Integer absent;
        private Integer late;
        private Integer excused;
        private Integer planned;
    }
}
