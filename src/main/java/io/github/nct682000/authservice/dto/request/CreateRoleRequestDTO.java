package io.github.nct682000.authservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateRoleRequestDTO {

    @NotBlank(message = "{auth.validation.role.name.required}")
    @Size(min = 3, max = 50, message = "{auth.validation.role.name.size}")
    private String name;

    @Size(max = 255, message = "{auth.validation.role.description.size}")
    private String description;
}
