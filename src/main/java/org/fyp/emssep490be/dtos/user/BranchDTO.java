package org.fyp.emssep490be.dtos.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BranchDTO {

    private Long id;

    private String code;

    private String name;

    private String address;

    private String location;
}
