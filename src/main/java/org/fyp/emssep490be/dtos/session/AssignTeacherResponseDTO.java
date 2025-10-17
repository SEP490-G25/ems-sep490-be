package org.fyp.emssep490be.dtos.session;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignTeacherResponseDTO {

    private Long sessionId;

    private Long teacherId;

    private String teacherName;

    private String skill;

    private String role;

    private LocalDateTime createdAt;
}
