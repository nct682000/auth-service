package io.github.nct682000.authservice.controller;

import io.github.nct682000.authservice.dto.APIResponse;
import io.github.nct682000.authservice.dto.request.LoginRequestDTO;
import io.github.nct682000.authservice.dto.request.RegisterRequestDTO;
import io.github.nct682000.authservice.dto.response.LoginResponseDTO;
import io.github.nct682000.authservice.dto.response.UserProfileResponseDTO;
import io.github.nct682000.authservice.enumeration.ResponseCode;
import io.github.nct682000.authservice.exception.AuthException;
import io.github.nct682000.authservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<APIResponse<UserProfileResponseDTO>> register(
            @Valid @RequestBody RegisterRequestDTO request) throws AuthException {

        UserProfileResponseDTO data = authService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.<UserProfileResponseDTO>builder()
                        .code(ResponseCode.REGISTER_SUCCESS.getCode())
                        .message(ResponseCode.REGISTER_SUCCESS.getMean())
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
                        .message(ResponseCode.LOGIN_SUCCESS.getMean())
                        .data(data)
                        .isSuccess(true)
                        .build());
    }
}
