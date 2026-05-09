package io.github.nct682000.authservice.controller;

import io.github.nct682000.authservice.dto.APIResponse;
import io.github.nct682000.authservice.dto.request.AssignPermissionsRequestDTO;
import io.github.nct682000.authservice.dto.request.AssignRolesRequestDTO;
import io.github.nct682000.authservice.dto.request.CreatePermissionRequestDTO;
import io.github.nct682000.authservice.dto.request.CreateRoleRequestDTO;
import io.github.nct682000.authservice.dto.request.PageRequests;
import io.github.nct682000.authservice.dto.request.UpdateUserStatusRequestDTO;
import io.github.nct682000.authservice.dto.response.PageResponseDTO;
import io.github.nct682000.authservice.dto.response.PermissionResponseDTO;
import io.github.nct682000.authservice.dto.response.RoleResponseDTO;
import io.github.nct682000.authservice.dto.response.RoleSummaryResponseDTO;
import io.github.nct682000.authservice.dto.response.UserProfileResponseDTO;
import io.github.nct682000.authservice.enumeration.ResponseCode;
import io.github.nct682000.authservice.exception.AuthException;
import io.github.nct682000.authservice.service.AdminService;
import io.github.nct682000.authservice.service.MessageResolver;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Validated
public class AdminController {

    private final AdminService adminService;
    private final MessageResolver messageResolver;

    @GetMapping("/users")
    @PreAuthorize("hasAuthority('profile:read:all')")
    public ResponseEntity<APIResponse<PageResponseDTO<UserProfileResponseDTO>>> getUsers(
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "{auth.validation.page.min}") int page,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "{auth.validation.size.min}") @Max(value = 100, message = "{auth.validation.size.max}") int size) {

        return ResponseEntity.ok(APIResponse.<PageResponseDTO<UserProfileResponseDTO>>builder()
                .code(ResponseCode.GET_PROFILE_SUCCESS.getCode())
                .message(messageResolver.resolve(ResponseCode.GET_PROFILE_SUCCESS))
                .data(adminService.getUsers(PageRequests.of(page, size)))
                .isSuccess(true)
                .build());
    }

    @GetMapping("/users/{userId}")
    @PreAuthorize("hasAuthority('profile:read:all')")
    public ResponseEntity<APIResponse<UserProfileResponseDTO>> getUser(
            @PathVariable UUID userId) throws AuthException {

        return ResponseEntity.ok(APIResponse.<UserProfileResponseDTO>builder()
                .code(ResponseCode.GET_PROFILE_SUCCESS.getCode())
                .message(messageResolver.resolve(ResponseCode.GET_PROFILE_SUCCESS))
                .data(adminService.getUser(userId))
                .isSuccess(true)
                .build());
    }

    @PatchMapping("/users/{userId}/status")
    @PreAuthorize("hasAuthority('profile:write:all')")
    public ResponseEntity<APIResponse<UserProfileResponseDTO>> updateUserStatus(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserStatusRequestDTO request) throws AuthException {

        return ResponseEntity.ok(APIResponse.<UserProfileResponseDTO>builder()
                .code(ResponseCode.USER_STATUS_UPDATED.getCode())
                .message(messageResolver.resolve(ResponseCode.USER_STATUS_UPDATED))
                .data(adminService.updateUserStatus(userId, request))
                .isSuccess(true)
                .build());
    }

    @PostMapping("/users/{userId}/roles")
    @PreAuthorize("hasAuthority('profile:write:all')")
    public ResponseEntity<APIResponse<UserProfileResponseDTO>> assignRolesToUser(
            @PathVariable UUID userId,
            @Valid @RequestBody AssignRolesRequestDTO request) throws AuthException {

        return ResponseEntity.ok(APIResponse.<UserProfileResponseDTO>builder()
                .code(ResponseCode.ROLE_ASSIGNED.getCode())
                .message(messageResolver.resolve(ResponseCode.ROLE_ASSIGNED))
                .data(adminService.assignRolesToUser(userId, request))
                .isSuccess(true)
                .build());
    }

    @DeleteMapping("/users/{userId}/roles/{roleId}")
    @PreAuthorize("hasAuthority('profile:write:all')")
    public ResponseEntity<APIResponse<UserProfileResponseDTO>> removeRoleFromUser(
            @PathVariable UUID userId,
            @PathVariable UUID roleId) throws AuthException {

        return ResponseEntity.ok(APIResponse.<UserProfileResponseDTO>builder()
                .code(ResponseCode.ROLE_REMOVED.getCode())
                .message(messageResolver.resolve(ResponseCode.ROLE_REMOVED))
                .data(adminService.removeRoleFromUser(userId, roleId))
                .isSuccess(true)
                .build());
    }

    @GetMapping("/roles")
    @PreAuthorize("hasAuthority('role:manage')")
    public ResponseEntity<APIResponse<PageResponseDTO<RoleSummaryResponseDTO>>> getRoles(
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "{auth.validation.page.min}") int page,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "{auth.validation.size.min}") @Max(value = 100, message = "{auth.validation.size.max}") int size) {

        return ResponseEntity.ok(APIResponse.<PageResponseDTO<RoleSummaryResponseDTO>>builder()
                .code(ResponseCode.GET_PROFILE_SUCCESS.getCode())
                .message(messageResolver.resolve(ResponseCode.GET_PROFILE_SUCCESS))
                .data(adminService.getRoles(PageRequests.of(page, size)))
                .isSuccess(true)
                .build());
    }

    @PostMapping("/roles")
    @PreAuthorize("hasAuthority('role:manage')")
    public ResponseEntity<APIResponse<RoleResponseDTO>> createRole(
            @Valid @RequestBody CreateRoleRequestDTO request) throws AuthException {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.<RoleResponseDTO>builder()
                        .code(ResponseCode.ROLE_CREATED.getCode())
                        .message(messageResolver.resolve(ResponseCode.ROLE_CREATED))
                        .data(adminService.createRole(request))
                        .isSuccess(true)
                        .build());
    }

    @DeleteMapping("/roles/{roleId}")
    @PreAuthorize("hasAuthority('role:manage')")
    public ResponseEntity<APIResponse<Void>> deleteRole(
            @PathVariable UUID roleId) throws AuthException {

        adminService.deleteRole(roleId);
        return ResponseEntity.ok(APIResponse.<Void>builder()
                .code(ResponseCode.ROLE_DELETED.getCode())
                .message(messageResolver.resolve(ResponseCode.ROLE_DELETED))
                .isSuccess(true)
                .build());
    }

    @PostMapping("/roles/{roleId}/permissions")
    @PreAuthorize("hasAuthority('role:manage')")
    public ResponseEntity<APIResponse<RoleResponseDTO>> assignPermissionsToRole(
            @PathVariable UUID roleId,
            @Valid @RequestBody AssignPermissionsRequestDTO request) throws AuthException {

        return ResponseEntity.ok(APIResponse.<RoleResponseDTO>builder()
                .code(ResponseCode.PERMISSION_ASSIGNED.getCode())
                .message(messageResolver.resolve(ResponseCode.PERMISSION_ASSIGNED))
                .data(adminService.assignPermissionsToRole(roleId, request))
                .isSuccess(true)
                .build());
    }

    @DeleteMapping("/roles/{roleId}/permissions/{permissionId}")
    @PreAuthorize("hasAuthority('role:manage')")
    public ResponseEntity<APIResponse<RoleResponseDTO>> removePermissionFromRole(
            @PathVariable UUID roleId,
            @PathVariable UUID permissionId) throws AuthException {

        return ResponseEntity.ok(APIResponse.<RoleResponseDTO>builder()
                .code(ResponseCode.PERMISSION_REMOVED.getCode())
                .message(messageResolver.resolve(ResponseCode.PERMISSION_REMOVED))
                .data(adminService.removePermissionFromRole(roleId, permissionId))
                .isSuccess(true)
                .build());
    }

    // ===== Permission management =====

    @GetMapping("/permissions")
    @PreAuthorize("hasAuthority('permission:manage')")
    public ResponseEntity<APIResponse<PageResponseDTO<PermissionResponseDTO>>> getPermissions(
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "{auth.validation.page.min}") int page,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "{auth.validation.size.min}") @Max(value = 100, message = "{auth.validation.size.max}") int size) {

        return ResponseEntity.ok(APIResponse.<PageResponseDTO<PermissionResponseDTO>>builder()
                .code(ResponseCode.GET_PROFILE_SUCCESS.getCode())
                .message(messageResolver.resolve(ResponseCode.GET_PROFILE_SUCCESS))
                .data(adminService.getPermissions(PageRequests.of(page, size)))
                .isSuccess(true)
                .build());
    }

    @PostMapping("/permissions")
    @PreAuthorize("hasAuthority('permission:manage')")
    public ResponseEntity<APIResponse<PermissionResponseDTO>> createPermission(
            @Valid @RequestBody CreatePermissionRequestDTO request) throws AuthException {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.<PermissionResponseDTO>builder()
                        .code(ResponseCode.PERMISSION_CREATED.getCode())
                        .message(messageResolver.resolve(ResponseCode.PERMISSION_CREATED))
                        .data(adminService.createPermission(request))
                        .isSuccess(true)
                        .build());
    }

    @DeleteMapping("/permissions/{permissionId}")
    @PreAuthorize("hasAuthority('permission:manage')")
    public ResponseEntity<APIResponse<Void>> deletePermission(
            @PathVariable UUID permissionId) throws AuthException {

        adminService.deletePermission(permissionId);
        return ResponseEntity.ok(APIResponse.<Void>builder()
                .code(ResponseCode.PERMISSION_DELETED.getCode())
                .message(messageResolver.resolve(ResponseCode.PERMISSION_DELETED))
                .isSuccess(true)
                .build());
    }
}
