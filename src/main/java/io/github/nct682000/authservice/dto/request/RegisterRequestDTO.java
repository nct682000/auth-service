package io.github.nct682000.authservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequestDTO {

    @NotBlank(message = "{auth.validation.username.required}")
    @Size(min = 3, max = 50, message = "{auth.validation.username.size}")
    private String username;

    @NotBlank(message = "{auth.validation.email.required}")
    @Email(message = "{auth.validation.email.invalid}")
    private String email;

    @NotBlank(message = "{auth.validation.password.required}")
    @Size(min = 8, message = "{auth.validation.password.size}")
    private String password;
}
