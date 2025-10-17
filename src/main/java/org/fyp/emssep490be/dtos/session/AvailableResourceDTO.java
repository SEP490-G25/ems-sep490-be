package org.fyp.emssep490be.dtos.session;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvailableResourceDTO {

    private Long resourceId;

    private String resourceType;

    private String resourceName;

    private Integer capacity;

    private Boolean isAvailable;

    private List<ResourceConflict> conflicts;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResourceConflict {
        private Long sessionId;
        private String classCode;
        private LocalTime startTime;
        private LocalTime endTime;
    }
}
