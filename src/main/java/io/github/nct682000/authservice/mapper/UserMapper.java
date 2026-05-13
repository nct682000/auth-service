package io.github.nct682000.authservice.mapper;

import io.github.nct682000.authservice.entity.AuthUserDetails;
import io.github.nct682000.authservice.entity.User;
import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class UserMapper {
    public static AuthUserDetails toUserDetails(User user) {
        List<SimpleGrantedAuthority> authorities =
                user.getRoles().stream()
                        .flatMap(role -> role.getPermissions().stream())
                        .map(permission -> new SimpleGrantedAuthority(permission.getName()))
                        .toList();

        return new AuthUserDetails(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                authorities,
                user.getStatus(),
                user.getTokenVersion(),
                user.getAccountExpiredAt(),
                user.getCredentialsExpiredAt(),
                user.getLockedUntil()
        );
    }
}
