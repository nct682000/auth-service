package io.github.nct682000.authservice.service;

import io.github.nct682000.authservice.dto.request.RegisterRequestDTO;
import io.github.nct682000.authservice.dto.response.UserProfileResponseDTO;
import io.github.nct682000.authservice.entity.Role;
import io.github.nct682000.authservice.entity.User;
import io.github.nct682000.authservice.enumeration.RoleEnum;
import io.github.nct682000.authservice.enumeration.UserStatus;
import io.github.nct682000.authservice.exception.AuthException;
import io.github.nct682000.authservice.exception.EmailAlreadyExistsException;
import io.github.nct682000.authservice.exception.RoleNotFoundException;
import io.github.nct682000.authservice.exception.UsernameAlreadyExistsException;
import io.github.nct682000.authservice.repository.RoleRepository;
import io.github.nct682000.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserProfileResponseDTO register(RegisterRequestDTO request)
            throws AuthException {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UsernameAlreadyExistsException();
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException();
        }

        Role defaultRole = roleRepository.findByName(RoleEnum.USER.getName())
                .orElseThrow(() -> new RoleNotFoundException(RoleEnum.USER.getName()));

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .status(UserStatus.ACTIVE)
                .roles(Set.of(defaultRole))
                .build();

        log.info("New user registered: {}", user.getUsername());
        return UserProfileResponseDTO.from(userRepository.save(user));
    }
}
