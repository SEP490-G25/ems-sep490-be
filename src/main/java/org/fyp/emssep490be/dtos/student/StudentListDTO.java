package org.fyp.emssep490be.dtos.student;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for student list/search operations
 * Lightweight version without nested objects
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentListDTO {
    
    private Long id;
    private String studentCode;
    private String fullName;
    private String email;
    private String phone;
    private Long branchId;
    private String branchName;
    private LocalDateTime createdAt;
}
