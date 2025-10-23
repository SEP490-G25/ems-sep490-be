package org.fyp.emssep490be.dtos.teacher;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeacherSkillsResponseDTO {

    private Long teacherId;
    private List<TeacherSkillDTO> skills;
    private OffsetDateTime updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeacherSkillDTO {
        private String skill;
        private Integer level;
    }
}
