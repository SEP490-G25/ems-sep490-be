package org.fyp.emssep490be.dtos.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDTO {

    private String email;

    private String phone;

    @NotBlank(message = "Password is required")
    private String password;
}
