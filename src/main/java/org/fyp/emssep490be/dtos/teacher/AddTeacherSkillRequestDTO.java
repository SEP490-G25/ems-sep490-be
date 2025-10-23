package org.fyp.emssep490be.dtos.teacher;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddTeacherSkillRequestDTO {
    
    @NotBlank(message = "Skill is required")
    private String skill;
    
    @NotNull(message = "Level is required")
    @Min(value = 1, message = "Level must be between 1 and 5")
    @Max(value = 5, message = "Level must be between 1 and 5")
    private Integer level;
}




