package io.github.nct682000.authservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogoutRequestDTO {

    @NotBlank(message = "{auth.validation.refresh.token.required}")
    private String refreshToken;
}
