package org.fyp.emssep490be.dtos.clo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.fyp.emssep490be.dtos.plo.PloDTO;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CloDTO {

    private Long id;

    private Long courseId;

    private String code;

    private String description;

    private List<PloDTO> mappedPlos;

    private Integer mappedSessionsCount;
}
