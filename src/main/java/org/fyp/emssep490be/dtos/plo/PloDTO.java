package org.fyp.emssep490be.dtos.plo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PloDTO {

    private Long id;

    private Long subjectId;

    private String code;

    private String description;

    private Integer mappedClosCount;
}
