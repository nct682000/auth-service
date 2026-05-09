package io.github.nct682000.authservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreatePermissionRequestDTO {

    @NotBlank(message = "{auth.validation.permission.name.required}")
    @Size(min = 3, max = 100, message = "{auth.validation.permission.name.size}")
    private String name;

    @Size(max = 255, message = "{auth.validation.permission.description.size}")
    private String description;
}
