package io.github.nct682000.authservice.entity;

import io.github.nct682000.authservice.enumeration.UserStatus;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.ObjectUtils;

@Getter
@AllArgsConstructor
public class AuthUserDetails implements UserDetails {
    private UUID userId;
    private String username;
    private String email;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;
    private UserStatus status;
    private Integer tokenVersion;
    private LocalDateTime accountExpiredAt;
    private LocalDateTime credentialsExpiredAt;
    private LocalDateTime lockedUntil;

    @Override
    public boolean isAccountNonLocked() {
        if (status != UserStatus.LOCKED) {
            return true;
        }
        return !ObjectUtils.isEmpty(lockedUntil) && lockedUntil.isBefore(LocalDateTime.now());
    }

    @Override
    public boolean isEnabled() {
        return status != UserStatus.DISABLED;
    }

    @Override
    public boolean isAccountNonExpired() {
        return ObjectUtils.isEmpty(accountExpiredAt) || accountExpiredAt.isAfter(
                LocalDateTime.now());
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return ObjectUtils.isEmpty(credentialsExpiredAt) || credentialsExpiredAt.isAfter(
                LocalDateTime.now());
    }
}
