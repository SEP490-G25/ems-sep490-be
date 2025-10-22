package org.fyp.emssep490be.dtos.coursesession;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCourseSessionRequestDTO {

    private String topic;

    private String studentTask;

    private List<String> skillSet;
}
