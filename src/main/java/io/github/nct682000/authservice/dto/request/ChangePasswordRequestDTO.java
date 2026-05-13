package io.github.nct682000.authservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequestDTO {

    @NotBlank(message = "{auth.validation.password.current.required}")
    private String currentPassword;

    @NotBlank(message = "{auth.validation.password.new.required}")
    @Size(min = 8, message = "{auth.validation.password.size}")
    private String newPassword;

    @NotBlank(message = "{auth.validation.password.confirm.required}")
    private String confirmPassword;
}
