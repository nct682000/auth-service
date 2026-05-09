package io.github.nct682000.authservice.dto.request;

import io.github.nct682000.authservice.enumeration.UserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUserStatusRequestDTO {

    @NotNull(message = "{auth.validation.status.required}")
    private UserStatus status;
}
