package org.fyp.emssep490be.dtos.studentrequest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.fyp.emssep490be.entities.enums.SessionStatus;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Basic session information for student request DTOs
 * Contains minimal session details needed for display
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionBasicDTO {

    private Long id;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String topic;
    private String className;
    private String branchName;
    private SessionStatus status;
    private Integer sequenceNo;
}
