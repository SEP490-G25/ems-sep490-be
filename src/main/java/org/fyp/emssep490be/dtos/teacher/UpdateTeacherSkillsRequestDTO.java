package org.fyp.emssep490be.dtos.teacher;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTeacherSkillsRequestDTO {

    @NotEmpty(message = "Skills list cannot be empty")
    @Valid
    private List<TeacherSkillDTO> skills;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeacherSkillDTO {
        @NotNull(message = "Skill is required")
        private String skill;

        @NotNull(message = "Level is required")
        private Integer level;
    }
}
