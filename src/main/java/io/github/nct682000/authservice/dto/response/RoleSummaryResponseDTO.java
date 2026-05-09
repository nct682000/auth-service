package io.github.nct682000.authservice.dto.response;

import io.github.nct682000.authservice.entity.Role;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class RoleSummaryResponseDTO {

    private UUID id;
    private String name;
    private String description;

    public static RoleSummaryResponseDTO from(Role role) {
        return RoleSummaryResponseDTO.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .build();
    }
}
