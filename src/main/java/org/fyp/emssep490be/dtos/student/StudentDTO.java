package org.fyp.emssep490be.dtos.student;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentDTO {
    private Long id;
    private Long userId;
    private String studentCode;
    private String fullName;
    private String email;
    private String phone;
    private Long branchId;
    private String branchName;
    private OffsetDateTime createdAt;
}
