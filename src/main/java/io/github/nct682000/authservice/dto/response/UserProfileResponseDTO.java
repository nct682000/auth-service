package io.github.nct682000.authservice.dto.response;

import io.github.nct682000.authservice.entity.Role;
import io.github.nct682000.authservice.entity.User;
import io.github.nct682000.authservice.enumeration.UserStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@Builder
public class UserProfileResponseDTO {

    private UUID id;
    private String username;
    private String email;
    private UserStatus status;
    private Set<String> roles;
    private LocalDateTime createdAt;

    public static UserProfileResponseDTO from(User user) {
        return UserProfileResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .status(user.getStatus())
                .roles(user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet()))
                .createdAt(user.getCreatedAt())
                .build();
    }
}
