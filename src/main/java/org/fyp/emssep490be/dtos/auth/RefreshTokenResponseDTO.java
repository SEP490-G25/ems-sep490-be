package org.fyp.emssep490be.dtos.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenResponseDTO {

    private String accessToken;

    private Integer expiresIn;
}
