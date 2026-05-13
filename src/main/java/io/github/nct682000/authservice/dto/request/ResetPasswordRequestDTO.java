package io.github.nct682000.authservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequestDTO {

    @NotBlank(message = "{auth.validation.email.required.forgot}")
    @Email(message = "{auth.validation.email.invalid}")
    private String email;

    @NotBlank(message = "{auth.validation.otp.required}")
    @Size(min = 6, max = 6, message = "{auth.validation.otp.size}")
    private String otp;

    @NotBlank(message = "{auth.validation.password.new.required}")
    @Size(min = 8, message = "{auth.validation.password.size}")
    private String newPassword;

    @NotBlank(message = "{auth.validation.password.confirm.required}")
    private String confirmPassword;
}
