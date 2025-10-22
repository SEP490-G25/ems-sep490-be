package org.fyp.emssep490be.dtos.subject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.fyp.emssep490be.dtos.level.LevelDTO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for Subject details including related levels
 * Used when fetching complete subject information with relationships
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectDetailDTO {

    private Long id;

    private String code;

    private String name;

    private String description;

    private String status;

    private Long createdBy;

    private LocalDateTime createdAt;

    private Integer levelsCount;

    private Integer coursesCount;

    /**
     * List of levels under this subject
     */
    private List<LevelDTO> levels;
}
