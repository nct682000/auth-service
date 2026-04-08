package io.github.nct682000.authservice.controller;

import io.github.nct682000.authservice.dto.APIResponse;
import io.github.nct682000.authservice.dto.request.LoginRequestDTO;
import io.github.nct682000.authservice.dto.request.LogoutRequestDTO;
import io.github.nct682000.authservice.dto.request.RefreshTokenRequestDTO;
import io.github.nct682000.authservice.dto.request.RegisterRequestDTO;
import io.github.nct682000.authservice.dto.response.LoginResponseDTO;
import io.github.nct682000.authservice.dto.response.UserProfileResponseDTO;
import io.github.nct682000.authservice.entity.AuthUserDetails;
import io.github.nct682000.authservice.enumeration.ResponseCode;
import io.github.nct682000.authservice.exception.AuthException;
import io.github.nct682000.authservice.service.AuthService;
import io.github.nct682000.authservice.service.MessageResolver;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final MessageResolver messageResolver;

    @PostMapping("/register")
    public ResponseEntity<APIResponse<UserProfileResponseDTO>> register(
            @Valid @RequestBody RegisterRequestDTO request) throws AuthException {

        UserProfileResponseDTO data = authService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.<UserProfileResponseDTO>builder()
                        .code(ResponseCode.REGISTER_SUCCESS.getCode())
                        .message(messageResolver.resolve(ResponseCode.REGISTER_SUCCESS))
                        .data(data)
                        .isSuccess(true)
                        .build());
    }

    @PostMapping("/login")
    public ResponseEntity<APIResponse<LoginResponseDTO>> login(
            @Valid @RequestBody LoginRequestDTO request) {

        LoginResponseDTO data = authService.login(request);

        return ResponseEntity.ok(
                APIResponse.<LoginResponseDTO>builder()
                        .code(ResponseCode.LOGIN_SUCCESS.getCode())
                        .message(messageResolver.resolve(ResponseCode.LOGIN_SUCCESS))
                        .data(data)
                        .isSuccess(true)
                        .build());
    }

    @PostMapping("/refresh")
    public ResponseEntity<APIResponse<LoginResponseDTO>> refresh(
            @Valid @RequestBody RefreshTokenRequestDTO request) throws AuthException {

        LoginResponseDTO data = authService.refresh(request);

        return ResponseEntity.ok(
                APIResponse.<LoginResponseDTO>builder()
                        .code(ResponseCode.TOKEN_REFRESHED.getCode())
                        .message(messageResolver.resolve(ResponseCode.TOKEN_REFRESHED))
                        .data(data)
                        .isSuccess(true)
                        .build());
    }

    @PostMapping("/logout")
    public ResponseEntity<APIResponse<Void>> logout(
            @Valid @RequestBody LogoutRequestDTO request,
            @AuthenticationPrincipal AuthUserDetails currentUser) throws AuthException {

        authService.logout(request, currentUser);

        return ResponseEntity.ok(
                APIResponse.<Void>builder()
                        .code(ResponseCode.LOGOUT_SUCCESS.getCode())
                        .message(messageResolver.resolve(ResponseCode.LOGOUT_SUCCESS))
                        .isSuccess(true)
                        .build());
    }

    @PostMapping("/logout-all")
    public ResponseEntity<APIResponse<Void>> logoutAll(
            @AuthenticationPrincipal AuthUserDetails currentUser) {

        authService.logoutAll(currentUser);

        return ResponseEntity.ok(
                APIResponse.<Void>builder()
                        .code(ResponseCode.LOGOUT_SUCCESS.getCode())
                        .message(messageResolver.resolve(ResponseCode.LOGOUT_SUCCESS))
                        .isSuccess(true)
                        .build());
    }
}
