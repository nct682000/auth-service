package io.github.nct682000.authservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordRequestDTO {

    @NotBlank(message = "{auth.validation.email.required.forgot}")
    @Email(message = "{auth.validation.email.invalid}")
    private String email;
}
