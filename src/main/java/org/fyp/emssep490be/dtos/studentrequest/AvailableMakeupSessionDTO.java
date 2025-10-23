package org.fyp.emssep490be.dtos.studentrequest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.fyp.emssep490be.entities.enums.Modality;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO representing an available makeup session.
 * Contains detailed information about a session that a student can attend
 * to make up for a missed session with the same course content.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailableMakeupSessionDTO {

    /**
     * ID of the makeup session
     */
    private Long sessionId;

    /**
     * ID of the class this session belongs to
     */
    private Long classId;

    /**
     * Name of the class (e.g., "English A1 Evening")
     */
    private String className;

    /**
     * ID of the branch where the class is held
     */
    private Long branchId;

    /**
     * Name of the branch (e.g., "Hoàn Kiếm")
     */
    private String branchName;

    /**
     * Modality of the class (OFFLINE, ONLINE, HYBRID)
     */
    private Modality modality;

    /**
     * Date of the session
     */
    private LocalDate date;

    /**
     * Start time of the session
     */
    private LocalTime startTime;

    /**
     * End time of the session
     */
    private LocalTime endTime;

    /**
     * Course session ID (must match the missed session's course_session_id)
     */
    private Long courseSessionId;

    /**
     * Topic/title of the session (e.g., "Listening Practice")
     */
    private String topic;

    /**
     * Sequence number of the session in the course
     */
    private Integer sequenceNo;

    /**
     * Number of available slots in this session
     */
    private Integer availableSlots;

    /**
     * Maximum capacity of the class
     */
    private Integer maxCapacity;

    /**
     * Current number of enrolled students
     */
    private Integer enrolledCount;
}
