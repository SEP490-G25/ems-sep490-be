package org.fyp.emssep490be.dtos.session;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignTeacherRequestDTO {

    @NotNull(message = "Teacher ID is required")
    private Long teacherId;

    @NotNull(message = "Skill is required")
    private String skill;

    @NotNull(message = "Role is required")
    private String role;
}
