package org.fyp.emssep490be.dtos.subject;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSubjectRequestDTO {

    private String name;

    private String description;

    private String status;
}
