package io.github.nct682000.authservice.dto.response;

import io.github.nct682000.authservice.entity.Permission;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class PermissionResponseDTO {

    private UUID id;
    private String name;
    private String description;

    public static PermissionResponseDTO from(Permission permission) {
        return PermissionResponseDTO.builder()
                .id(permission.getId())
                .name(permission.getName())
                .description(permission.getDescription())
                .build();
    }
}
