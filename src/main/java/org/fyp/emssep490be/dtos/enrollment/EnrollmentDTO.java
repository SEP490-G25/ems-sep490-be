package org.fyp.emssep490be.dtos.enrollment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentDTO {
    private Long id;
    private Long studentId;
    private Long classId;
    private String classCode;
    private String className;
    private LocalDateTime enrolledAt;
    private String status;
    private Double progressPercentage;
}
