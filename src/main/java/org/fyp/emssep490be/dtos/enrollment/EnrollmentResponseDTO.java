package org.fyp.emssep490be.dtos.enrollment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for enrollment operations
 * Contains basic enrollment information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentResponseDTO {

    private Long enrollmentId;
    private Long classId;
    private Long studentId;
    private String enrollmentStatus;
    private LocalDateTime enrolledAt;
    private LocalDateTime completedAt;
}
