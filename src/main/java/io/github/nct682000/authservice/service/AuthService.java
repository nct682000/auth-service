package io.github.nct682000.authservice.service;

import io.github.nct682000.authservice.config.AuthSecurityConfig;
import io.github.nct682000.authservice.config.JwtConfig;
import io.github.nct682000.authservice.dto.request.ChangePasswordRequestDTO;
import io.github.nct682000.authservice.dto.request.ForgotPasswordRequestDTO;
import io.github.nct682000.authservice.dto.request.LoginRequestDTO;
import io.github.nct682000.authservice.dto.request.LogoutRequestDTO;
import io.github.nct682000.authservice.dto.request.RefreshTokenRequestDTO;
import io.github.nct682000.authservice.dto.request.RegisterRequestDTO;
import io.github.nct682000.authservice.dto.request.ResetPasswordRequestDTO;
import io.github.nct682000.authservice.dto.response.LoginResponseDTO;
import io.github.nct682000.authservice.dto.response.UserProfileResponseDTO;
import io.github.nct682000.authservice.entity.AuthUserDetails;
import io.github.nct682000.authservice.entity.Role;
import io.github.nct682000.authservice.entity.User;
import io.github.nct682000.authservice.enumeration.AccountPolicy;
import io.github.nct682000.authservice.enumeration.ResponseCode;
import io.github.nct682000.authservice.enumeration.RoleEnum;
import io.github.nct682000.authservice.enumeration.UserStatus;
import io.github.nct682000.authservice.exception.AuthException;
import io.github.nct682000.authservice.exception.InvalidOtpException;
import io.github.nct682000.authservice.exception.InvalidPasswordException;
import io.github.nct682000.authservice.exception.InvalidTokenException;
import io.github.nct682000.authservice.exception.RevokedTokenException;
import io.github.nct682000.authservice.exception.RoleNotFoundException;
import io.github.nct682000.authservice.exception.UserNotFoundException;
import io.github.nct682000.authservice.mapper.UserMapper;
import io.github.nct682000.authservice.repository.RoleRepository;
import io.github.nct682000.authservice.repository.UserRepository;
import io.github.nct682000.authservice.service.JwtService.RefreshTokenResult;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
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
    private final AuthSecurityConfig securityConfig;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final LoginAttemptService loginAttemptService;
    private final MailSender mailSender;

    private static final SecureRandom OTP_RANDOM = new SecureRandom();

    @Transactional
    public UserProfileResponseDTO register(RegisterRequestDTO request) throws AuthException {
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
        Optional<User> existingUser = userRepository.findByUsername(request.getUsername());
        existingUser.ifPresent(loginAttemptService::checkLockout);

        Authentication auth;
        try {
            auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(),
                            request.getPassword()));
        } catch (BadCredentialsException ex) {
            // Failed authentication for a username we recognize → record the
            // failure and let the lockout policy decide whether to lock.
            // We re-throw the same BadCredentialsException so the response
            // stays 401 INVALID_CREDENTIALS even on the attempt that triggers
            // the lock — leaking ACCOUNT_LOCKED on the trigger attempt would
            // help an attacker enumerate the threshold.
            existingUser.ifPresent(loginAttemptService::recordFailure);
            throw ex;
        }

        AuthUserDetails userDetails = (AuthUserDetails) auth.getPrincipal();

        // Successful authentication — clear any partial failure counter.
        loginAttemptService.recordSuccess(userDetails.getUsername());

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
                .orElseThrow(() -> new UserNotFoundException(authClaims.userId().toString()));

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

    @Transactional
    public void changePassword(AuthUserDetails currentUser, ChangePasswordRequestDTO request)
            throws AuthException {

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new AuthException(ResponseCode.PASSWORD_CONFIRMATION_MISMATCH);
        }

        // Re-load the user from DB. The principal carries fields from when the
        // JWT was issued and may be stale on tokenVersion (e.g. after a recent
        // logout-all). The DB row is the source of truth for the bump below.
        User user = userRepository.findById(currentUser.getUserId())
                .orElseThrow(() -> new UserNotFoundException(currentUser.getUserId().toString()));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidPasswordException();
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new AuthException(ResponseCode.PASSWORD_SAME_AS_CURRENT);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setCredentialsExpiredAt(AccountPolicy.CREDENTIALS_90_DAYS.expiresAt());
        userRepository.save(user);

        // Bump token_version — invalidates every existing access token via the
        // version check in JwtAuthenticationFilter.
        userRepository.incrementTokenVersion(user.getId());

        log.info("Password changed for user: {}", user.getUsername());
    }

    public void forgotPassword(ForgotPasswordRequestDTO request) {
        userRepository.findByEmail(request.getEmail()).ifPresentOrElse(
                user -> {
                    String otp = generateOtp();
                    String hashedOtp = passwordEncoder.encode(otp);
                    redisService.saveOtp(user.getEmail(), hashedOtp, securityConfig.getOtpTtl());
                    mailSender.sendPasswordResetOtp(user.getEmail(), otp);
                    log.info("Password reset OTP issued for {}", user.getUsername());
                },
                () -> log.info("Password reset requested for unknown email — silently ignored"));
    }

    @Transactional
    public void resetPassword(ResetPasswordRequestDTO request) throws AuthException {

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new AuthException(ResponseCode.PASSWORD_CONFIRMATION_MISMATCH);
        }

        String hashedOtp = redisService.getOtp(request.getEmail())
                .orElseThrow(InvalidOtpException::new);

        if (!passwordEncoder.matches(request.getOtp(), hashedOtp)) {
            throw new InvalidOtpException();
        }

        // Single-use: remove the OTP before mutating user state.
        redisService.deleteOtp(request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException(request.getEmail()));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setCredentialsExpiredAt(AccountPolicy.CREDENTIALS_90_DAYS.expiresAt());
        userRepository.save(user);

        userRepository.incrementTokenVersion(user.getId());

        log.info("Password reset for user: {}", user.getUsername());
    }

    /**
     * Generates a 6-digit numeric OTP using {@link SecureRandom}.
     * Zero-padded so the value is always exactly 6 characters.
     */
    private String generateOtp() {
        int code = OTP_RANDOM.nextInt(1_000_000);
        return String.format("%06d", code);
    }
}
