package org.fyp.emssep490be.dtos.teacher;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeacherProfileDTO {
    private Long id;
    private Long userAccountId;
    private String employeeCode;
    private String fullName;
    private String email;
    private String phone;
    private String status;
    private List<TeacherSkillDTO> skills;
    private List<TeacherAvailabilityDTO> availability;
}
