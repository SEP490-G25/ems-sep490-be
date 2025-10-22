package org.fyp.emssep490be.dtos.student;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO for student schedule session details
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentScheduleSessionDTO {
    
    // StudentSession info
    // Note: Composite key can be constructed as studentId + "-" + sessionId if needed
    private String attendanceStatus;
    private Boolean isMakeup;
    private String note;
    
    // Session info
    private Long sessionId;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    
    // Class info
    private Long classId;
    private String classCode;
    private String className;
    
    // CourseSession info
    private Integer sequenceNo;
    private String topic;
    
    // Teacher info
    private String teacherName;
    
    // Resource info
    private String resourceType;
    private String resourceName;
}
