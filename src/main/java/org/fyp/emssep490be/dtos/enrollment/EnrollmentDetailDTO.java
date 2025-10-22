package org.fyp.emssep490be.dtos.enrollment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Detailed enrollment information including student and class details
 * Used for displaying enrollment lists with complete context
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentDetailDTO {

    private Long enrollmentId;
    
    // Student information
    private Long studentId;
    private String studentCode;
    private String studentFullName;
    private String studentEmail;
    private String studentPhone;
    
    // Class information
    private Long classId;
    private String classCode;
    private String className;
    private String courseCode;
    private String courseName;
    private String branchName;
    
    // Enrollment details
    private String enrollmentStatus;
    private LocalDateTime enrolledAt;
    private LocalDateTime completedAt;
    private LocalDateTime leftAt;
}
