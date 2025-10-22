package org.fyp.emssep490be.dtos.student;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStudentRequestDTO {
    
    private String fullName;
    
    @Email(message = "Email must be valid")
    private String email;
    
    private String phone;
    
    private Long branchId;
}
