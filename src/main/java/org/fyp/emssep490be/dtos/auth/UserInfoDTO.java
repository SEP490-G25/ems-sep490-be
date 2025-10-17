package org.fyp.emssep490be.dtos.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDTO {

    private Long id;

    private String email;

    private String fullName;

    private List<String> roles;

    private List<BranchInfoDTO> branches;
}
