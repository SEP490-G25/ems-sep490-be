package org.fyp.emssep490be.dtos.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BranchInfoDTO {

    private Long id;

    private String code;

    private String name;
}
