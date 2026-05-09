package io.github.nct682000.authservice.dto.response;

import io.github.nct682000.authservice.entity.Role;
import lombok.Builder;
import lombok.Data;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@Builder
public class RoleResponseDTO {

    private UUID id;
    private String name;
    private String description;
    private Set<PermissionResponseDTO> permissions;

    public static RoleResponseDTO from(Role role) {
        return RoleResponseDTO.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .permissions(role.getPermissions().stream()
                        .map(PermissionResponseDTO::from)
                        .collect(Collectors.toSet()))
                .build();
    }
}
