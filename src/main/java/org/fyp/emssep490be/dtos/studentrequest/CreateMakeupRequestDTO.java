package org.fyp.emssep490be.dtos.studentrequest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a makeup request.
 * Used when a student wants to make up a missed session by attending
 * the same content (course_session_id) in a different class.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMakeupRequestDTO {

    /**
     * The session that the student missed or will miss.
     * This is the original session from the student's enrolled class.
     */
    @NotNull(message = "Target session ID is required")
    private Long targetSessionId;

    /**
     * The session that the student wants to attend as a makeup.
     * Must have the same course_session_id as the target session.
     */
    @NotNull(message = "Makeup session ID is required")
    private Long makeupSessionId;

    /**
     * Reason for the makeup request.
     * Example: "Make up for missed Listening Practice session on Feb 10"
     */
    @NotBlank(message = "Reason is required")
    @Size(min = 10, max = 500, message = "Reason must be between 10 and 500 characters")
    private String reason;
}
