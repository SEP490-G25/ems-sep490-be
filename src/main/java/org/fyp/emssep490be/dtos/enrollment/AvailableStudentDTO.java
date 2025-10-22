package org.fyp.emssep490be.dtos.enrollment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for students available to enroll in a class
 * Used for filtering and displaying students that can be added to a class
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvailableStudentDTO {

    private Long studentId;
    private String studentCode;
    private String fullName;
    private String email;
    private String phone;
    private String branchName;
    private Boolean isEnrolled; // false = available, true = already enrolled
}
