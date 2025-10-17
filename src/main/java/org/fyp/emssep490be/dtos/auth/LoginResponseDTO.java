package org.fyp.emssep490be.dtos.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {

    private String accessToken;

    private String refreshToken;

    private Integer expiresIn;

    private UserInfoDTO user;
}
