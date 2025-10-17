package org.fyp.emssep490be.dtos.classmanagement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassValidationResponseDTO {

    private Boolean isValid;

    private List<Conflict> conflicts;

    private List<Warning> warnings;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Conflict {
        private String type;
        private Long sessionId;
        private LocalDate date;
        private LocalTime startTime;
        private LocalTime endTime;
        private Long resourceId;
        private String resourceName;
        private Long conflictingClassId;
        private String conflictingClassCode;
        private Long teacherId;
        private String teacherName;
        private Long conflictingSessionId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Warning {
        private String type;
        private Long sessionId;
        private Integer resourceCapacity;
        private Integer enrolledStudents;
        private String message;
    }
}
