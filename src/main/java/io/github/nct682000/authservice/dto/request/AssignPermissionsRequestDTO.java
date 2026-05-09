package io.github.nct682000.authservice.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
public class AssignPermissionsRequestDTO {

    @NotEmpty(message = "{auth.validation.permissions.required}")
    private Set<UUID> permissionIds;
}
