package org.fyp.emssep490be.dtos.teacher;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeacherSkillDTO {
    private Long teacherId;
    private String skill;
    private Integer proficiencyLevel;
}
