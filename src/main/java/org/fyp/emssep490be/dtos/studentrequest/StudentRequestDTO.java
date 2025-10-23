package org.fyp.emssep490be.dtos.studentrequest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.fyp.emssep490be.entities.enums.RequestStatus;
import org.fyp.emssep490be.entities.enums.StudentRequestType;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Main DTO for Student Request responses
 * Used for all request types (ABSENCE, MAKEUP, TRANSFER)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentRequestDTO {

    private Long id;
    private StudentRequestType requestType;
    private RequestStatus status;

    // Student information
    private Long studentId;
    private String studentName;
    private String studentEmail;

    // Request details
    private Long targetSessionId;
    private SessionBasicDTO targetSession;

    private Long makeupSessionId;
    private SessionBasicDTO makeupSession;

    private Long currentClassId;
    private String currentClassName;

    private Long targetClassId;
    private String targetClassName;

    private LocalDate effectiveDate;
    private String reason;

    // Submission tracking
    private LocalDateTime submittedAt;
    private Long submittedBy;
    private String submittedByName;

    // Decision tracking
    private LocalDateTime decidedAt;
    private Long decidedBy;
    private String decidedByName;
    private String resolution;

    // Additional info
    private String note;
}
