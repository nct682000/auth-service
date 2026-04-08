package io.github.nct682000.authservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDTO {

    @NotBlank(message = "{auth.validation.username.required}")
    private String username;

    @NotBlank(message = "{auth.validation.password.required}")
    private String password;
}
