package org.fyp.emssep490be.dtos.enrollment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Response DTO for batch enrollment operation
 * Supports partial success pattern - tracks both successful and failed enrollments
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchEnrollResponseDTO {

    private int totalRequested;
    private int successfulCount;
    private int failedCount;
    
    @Builder.Default
    private List<EnrollmentResponseDTO> successful = new ArrayList<>();
    
    @Builder.Default
    private List<EnrollmentError> failed = new ArrayList<>();
    
    /**
     * Inner class to capture enrollment failure details
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EnrollmentError {
        private Long studentId;
        private String studentCode;
        private String studentName;
        private String reason;
    }
}
