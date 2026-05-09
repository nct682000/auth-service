package io.github.nct682000.authservice.service;

import io.github.nct682000.authservice.dto.request.AssignPermissionsRequestDTO;
import io.github.nct682000.authservice.dto.request.AssignRolesRequestDTO;
import io.github.nct682000.authservice.dto.request.CreatePermissionRequestDTO;
import io.github.nct682000.authservice.dto.request.CreateRoleRequestDTO;
import io.github.nct682000.authservice.dto.request.UpdateUserStatusRequestDTO;
import io.github.nct682000.authservice.dto.response.PageResponseDTO;
import io.github.nct682000.authservice.dto.response.PermissionResponseDTO;
import io.github.nct682000.authservice.dto.response.RoleResponseDTO;
import io.github.nct682000.authservice.dto.response.RoleSummaryResponseDTO;
import io.github.nct682000.authservice.dto.response.UserProfileResponseDTO;
import io.github.nct682000.authservice.entity.Permission;
import io.github.nct682000.authservice.entity.Role;
import io.github.nct682000.authservice.entity.User;
import io.github.nct682000.authservice.exception.AuthException;
import io.github.nct682000.authservice.exception.PermissionNotFoundException;
import io.github.nct682000.authservice.exception.RoleNotFoundException;
import io.github.nct682000.authservice.exception.UserNotFoundException;
import io.github.nct682000.authservice.repository.PermissionRepository;
import io.github.nct682000.authservice.repository.RoleRepository;
import io.github.nct682000.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    // ===== User management =====

    @Transactional(readOnly = true)
    public PageResponseDTO<UserProfileResponseDTO> getUsers(Pageable pageable) {
        Page<UserProfileResponseDTO> page = userRepository.findAll(pageable)
                .map(UserProfileResponseDTO::from);
        return PageResponseDTO.from(page);
    }

    @Transactional(readOnly = true)
    public UserProfileResponseDTO getUser(UUID userId) throws AuthException {
        return userRepository.findById(userId)
                .map(UserProfileResponseDTO::from)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));
    }

    @Transactional
    public UserProfileResponseDTO updateUserStatus(UUID userId, UpdateUserStatusRequestDTO request) throws AuthException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));
        user.setStatus(request.getStatus());
        log.info("Admin updated status of user {} to {}", userId, request.getStatus());
        return UserProfileResponseDTO.from(userRepository.save(user));
    }

    @Transactional
    public UserProfileResponseDTO assignRolesToUser(UUID userId, AssignRolesRequestDTO request) throws AuthException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));

        Set<Role> roles = new HashSet<>(roleRepository.findAllById(request.getRoleIds()));
        if (roles.size() != request.getRoleIds().size()) {
            throw new RoleNotFoundException(request.getRoleIds().toString());
        }

        user.getRoles().addAll(roles);
        log.info("Admin assigned roles {} to user {}", request.getRoleIds(), userId);
        return UserProfileResponseDTO.from(userRepository.save(user));
    }

    @Transactional
    public UserProfileResponseDTO removeRoleFromUser(UUID userId, UUID roleId) throws AuthException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RoleNotFoundException(roleId.toString()));

        user.getRoles().remove(role);
        log.info("Admin removed role {} from user {}", roleId, userId);
        return UserProfileResponseDTO.from(userRepository.save(user));
    }

    // ===== Role management =====

    @Transactional(readOnly = true)
    public PageResponseDTO<RoleSummaryResponseDTO> getRoles(Pageable pageable) {
        Page<RoleSummaryResponseDTO> page = roleRepository.findAll(pageable)
                .map(RoleSummaryResponseDTO::from);
        return PageResponseDTO.from(page);
    }

    @Transactional
    public RoleResponseDTO createRole(CreateRoleRequestDTO request) throws AuthException {
        // Uniqueness of role.name is enforced by uk_role_name_active (V10).
        // Duplicates are mapped to AUTH-031-409 by GlobalExceptionHandler at commit time.
        Role role = new Role();
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        role.setPermissions(new HashSet<>());
        log.info("Admin created role: {}", request.getName());
        return RoleResponseDTO.from(roleRepository.save(role));
    }

    @Transactional
    public void deleteRole(UUID roleId) throws AuthException {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RoleNotFoundException(roleId.toString()));
        role.setDeletedAt(LocalDateTime.now());
        roleRepository.save(role);
        log.info("Admin soft-deleted role: {}", roleId);
    }

    @Transactional
    public RoleResponseDTO assignPermissionsToRole(UUID roleId, AssignPermissionsRequestDTO request) throws AuthException {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RoleNotFoundException(roleId.toString()));

        List<Permission> permissions = permissionRepository.findAllById(request.getPermissionIds());
        if (permissions.size() != request.getPermissionIds().size()) {
            throw new PermissionNotFoundException(request.getPermissionIds().toString());
        }

        role.getPermissions().addAll(permissions);
        log.info("Admin assigned permissions {} to role {}", request.getPermissionIds(), roleId);
        return RoleResponseDTO.from(roleRepository.save(role));
    }

    @Transactional
    public RoleResponseDTO removePermissionFromRole(UUID roleId, UUID permissionId) throws AuthException {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RoleNotFoundException(roleId.toString()));
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new PermissionNotFoundException(permissionId.toString()));

        role.getPermissions().remove(permission);
        log.info("Admin removed permission {} from role {}", permissionId, roleId);
        return RoleResponseDTO.from(roleRepository.save(role));
    }

    // ===== Permission management =====

    @Transactional(readOnly = true)
    public PageResponseDTO<PermissionResponseDTO> getPermissions(Pageable pageable) {
        Page<PermissionResponseDTO> page = permissionRepository.findAll(pageable)
                .map(PermissionResponseDTO::from);
        return PageResponseDTO.from(page);
    }

    @Transactional
    public PermissionResponseDTO createPermission(CreatePermissionRequestDTO request) throws AuthException {
        // Uniqueness of permission.name is enforced by uk_permission_name_active (V10).
        // Duplicates are mapped to AUTH-036-409 by GlobalExceptionHandler at commit time.
        Permission permission = new Permission();
        permission.setName(request.getName());
        permission.setDescription(request.getDescription());
        log.info("Admin created permission: {}", request.getName());
        return PermissionResponseDTO.from(permissionRepository.save(permission));
    }

    @Transactional
    public void deletePermission(UUID permissionId) throws AuthException {
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new PermissionNotFoundException(permissionId.toString()));
        permission.setDeletedAt(LocalDateTime.now());
        permissionRepository.save(permission);
        log.info("Admin soft-deleted permission: {}", permissionId);
    }
}
