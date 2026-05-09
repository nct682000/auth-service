package io.github.nct682000.authservice.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
public class AssignRolesRequestDTO {

    @NotEmpty(message = "{auth.validation.roles.required}")
    private Set<UUID> roleIds;
}
