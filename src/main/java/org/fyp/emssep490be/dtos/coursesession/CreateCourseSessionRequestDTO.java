package org.fyp.emssep490be.dtos.coursesession;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCourseSessionRequestDTO {

    @NotNull(message = "Sequence number is required")
    @Positive(message = "Sequence number must be positive")
    private Integer sequenceNo;

    private String topic;

    private String studentTask;

    private List<String> skillSet;
}
