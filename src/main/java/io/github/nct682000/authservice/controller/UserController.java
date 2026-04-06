package io.github.nct682000.authservice.controller;

import io.github.nct682000.authservice.dto.APIResponse;
import io.github.nct682000.authservice.dto.response.UserProfileResponseDTO;
import io.github.nct682000.authservice.entity.AuthUserDetails;
import io.github.nct682000.authservice.enumeration.ResponseCode;
import io.github.nct682000.authservice.exception.AuthException;
import io.github.nct682000.authservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<APIResponse<UserProfileResponseDTO>> getMe(
            @AuthenticationPrincipal AuthUserDetails currentUser) throws AuthException {

        UserProfileResponseDTO data = userService.getMe(currentUser);

        return ResponseEntity.ok(
                APIResponse.<UserProfileResponseDTO>builder()
                        .code(ResponseCode.GET_PROFILE_SUCCESS.getCode())
                        .message(ResponseCode.GET_PROFILE_SUCCESS.getMean())
                        .data(data)
                        .isSuccess(true)
                        .build());
    }
}
