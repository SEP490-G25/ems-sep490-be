package org.fyp.emssep490be.dtos.session;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.fyp.emssep490be.dtos.coursematerial.CourseMaterialDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionDetailDTO {

    private Long id;

    private ClassInfo classInfo;

    private CourseSessionDetail courseSession;

    private LocalDate date;

    private LocalTime startTime;

    private LocalTime endTime;

    private String type;

    private String status;

    private List<TeacherInfo> teachers;

    private List<ResourceInfo> resources;

    private List<StudentInfo> students;

    private String teacherNote;

    private LocalDateTime createdAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClassInfo {
        private Long id;
        private String code;
        private String name;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseSessionDetail {
        private Long id;
        private Integer sequenceNo;
        private String topic;
        private String studentTask;
        private List<String> skillSet;
        private List<CourseMaterialDTO> materials;
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
    public static class StudentInfo {
        private Long studentId;
        private String studentCode;
        private String studentName;
        private Boolean isMakeup;
        private String attendanceStatus;
    }
}
