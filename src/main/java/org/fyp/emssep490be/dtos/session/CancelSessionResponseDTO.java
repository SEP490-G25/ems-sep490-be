package org.fyp.emssep490be.dtos.session;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CancelSessionResponseDTO {

    private Long id;

    private String status;

    private String teacherNote;

    private Integer studentsNotified;

    private LocalDateTime updatedAt;
}
