package io.github.nct682000.authservice.service;

import io.github.nct682000.authservice.config.JwtConfig;
import io.github.nct682000.authservice.dto.request.LoginRequestDTO;
import io.github.nct682000.authservice.dto.request.LogoutRequestDTO;
import io.github.nct682000.authservice.dto.request.RefreshTokenRequestDTO;
import io.github.nct682000.authservice.dto.request.RegisterRequestDTO;
import io.github.nct682000.authservice.dto.response.LoginResponseDTO;
import io.github.nct682000.authservice.dto.response.UserProfileResponseDTO;
import io.github.nct682000.authservice.entity.AuthUserDetails;
import io.github.nct682000.authservice.entity.Role;
import io.github.nct682000.authservice.entity.User;
import io.github.nct682000.authservice.enumeration.AccountPolicy;
import io.github.nct682000.authservice.enumeration.RoleEnum;
import io.github.nct682000.authservice.enumeration.UserStatus;
import io.github.nct682000.authservice.exception.AuthException;
import io.github.nct682000.authservice.exception.EmailAlreadyExistsException;
import io.github.nct682000.authservice.exception.InvalidTokenException;
import io.github.nct682000.authservice.exception.RevokedTokenException;
import io.github.nct682000.authservice.exception.RoleNotFoundException;
import io.github.nct682000.authservice.exception.UserNotFoundException;
import io.github.nct682000.authservice.exception.UsernameAlreadyExistsException;
import io.github.nct682000.authservice.mapper.UserMapper;
import io.github.nct682000.authservice.repository.RoleRepository;
import io.github.nct682000.authservice.repository.UserRepository;
import io.github.nct682000.authservice.service.JwtService.RefreshTokenResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RedisService redisService;
    private final JwtConfig jwtConfig;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserProfileResponseDTO register(RegisterRequestDTO request) throws AuthException {
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
                .accountExpiredAt(AccountPolicy.NEVER.expiresAt())
                .credentialsExpiredAt(AccountPolicy.CREDENTIALS_90_DAYS.expiresAt())
                .build();

        log.info("New user registered: {}", user.getUsername());
        return UserProfileResponseDTO.from(userRepository.save(user));
    }

    public LoginResponseDTO login(LoginRequestDTO request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        AuthUserDetails userDetails = (AuthUserDetails) auth.getPrincipal();

        String accessToken = jwtService.generateAccessToken(userDetails);
        RefreshTokenResult refreshResult = jwtService.generateRefreshToken(userDetails);

        redisService.saveRefreshToken(
                userDetails.getUserId(),
                refreshResult.tokenId(),
                refreshResult.token(),
                Duration.ofMillis(jwtConfig.getRefreshTokenExpiration())
        );

        log.info("User logged in: {}", userDetails.getUsername());

        return LoginResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshResult.token())
                .expiresIn(jwtConfig.getAccessTokenExpiration() / 1000)
                .build();
    }

    @Transactional(readOnly = true)
    public LoginResponseDTO refresh(RefreshTokenRequestDTO request) throws AuthException {
        AuthClaims authClaims = jwtService.parseToken(request.getRefreshToken())
                .orElseThrow(InvalidTokenException::new);

        // Verify the token hasn't been revoked (still exists in Redis)
        redisService.getRefreshToken(authClaims.userId(), authClaims.tokenId())
                .orElseThrow(RevokedTokenException::new);

        // Load user from DB (picks up any status/role/version changes)
        User user = userRepository.findById(authClaims.userId())
                .orElseThrow(UserNotFoundException::new);

        AuthUserDetails userDetails = UserMapper.toUserDetails(user);

        // Token rotation: invalidate old, issue new pair
        redisService.deleteRefreshToken(authClaims.userId(), authClaims.tokenId());
        String newAccessToken = jwtService.generateAccessToken(userDetails);
        RefreshTokenResult newRefresh = jwtService.generateRefreshToken(userDetails);
        redisService.saveRefreshToken(
                authClaims.userId(), newRefresh.tokenId(), newRefresh.token(),
                Duration.ofMillis(jwtConfig.getRefreshTokenExpiration())
        );

        log.info("Token refreshed for user: {}", userDetails.getUsername());
        return LoginResponseDTO.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefresh.token())
                .expiresIn(jwtConfig.getAccessTokenExpiration() / 1000)
                .build();
    }

    public void logout(LogoutRequestDTO request, AuthUserDetails currentUser) throws AuthException {
        AuthClaims authClaims = jwtService.parseToken(request.getRefreshToken())
                .orElseThrow(InvalidTokenException::new);

        redisService.deleteRefreshToken(currentUser.getUserId(), authClaims.tokenId());
        log.info("User logged out: {}", currentUser.getUsername());
    }

    @Transactional
    public void logoutAll(AuthUserDetails currentUser) {
        redisService.deleteAllRefreshTokens(currentUser.getUserId());
        userRepository.incrementTokenVersion(currentUser.getUserId());
        log.info("All sessions revoked for user: {}", currentUser.getUsername());
    }
}
