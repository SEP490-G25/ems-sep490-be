package org.fyp.emssep490be.services.user.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fyp.emssep490be.dtos.common.PagedResponseDTO;
import org.fyp.emssep490be.dtos.common.PaginationDTO;
import org.fyp.emssep490be.dtos.user.*;
import org.fyp.emssep490be.entities.*;
import org.fyp.emssep490be.entities.ids.UserBranchId;
import org.fyp.emssep490be.entities.ids.UserRoleId;
import org.fyp.emssep490be.exceptions.CustomException;
import org.fyp.emssep490be.exceptions.ErrorCode;
import org.fyp.emssep490be.repositories.*;
import org.fyp.emssep490be.services.user.UserAccountService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of UserAccountService for user management operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserAccountServiceImpl implements UserAccountService {

    private final UserAccountRepository userAccountRepository;
    private final RoleRepository roleRepository;
    private final BranchRepository branchRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserBranchRepository userBranchRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDTO<UserDTO> getAllUsers(String status, Long roleId, Long branchId, Integer page, Integer limit) {
        log.info("Getting all users: status={}, roleId={}, branchId={}, page={}, limit={}", status, roleId, branchId, page, limit);

        int pageNumber = (page != null && page > 0) ? page - 1 : 0;
        int pageSize = (limit != null && limit > 0) ? limit : 10;

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("id").descending());

        Page<UserAccount> userPage;

        // TODO: Implement filtering by status, roleId, branchId with custom queries
        // For now, just return all users
        userPage = userAccountRepository.findAll(pageable);

        List<UserDTO> userDTOs = userPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        PaginationDTO pagination = new PaginationDTO(
                userPage.getNumber() + 1,
                userPage.getTotalPages(),
                userPage.getTotalElements(),
                userPage.getSize()
        );

        return new PagedResponseDTO<>(userDTOs, pagination);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        log.info("Getting user by ID: {}", id);

        UserAccount user = userAccountRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return convertToDTO(user);
    }

    @Override
    public UserDTO createUser(CreateUserRequestDTO request) {
        log.info("Creating user: {}", request.getEmail());

        // Validate email uniqueness
        if (userAccountRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(ErrorCode.USER_EMAIL_ALREADY_EXISTS);
        }

        // Validate phone uniqueness if provided
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            if (userAccountRepository.existsByPhone(request.getPhone())) {
                throw new CustomException(ErrorCode.USER_PHONE_ALREADY_EXISTS);
            }
        }

        // Validate roles exist
        List<Role> roles = new ArrayList<>();
        for (Long roleId : request.getRoleIds()) {
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new CustomException(ErrorCode.ROLE_NOT_FOUND));
            roles.add(role);
        }

        // Validate branches exist if provided
        List<Branch> branches = new ArrayList<>();
        if (request.getBranchIds() != null && !request.getBranchIds().isEmpty()) {
            for (Long branchId : request.getBranchIds()) {
                Branch branch = branchRepository.findById(branchId)
                        .orElseThrow(() -> new CustomException(ErrorCode.BRANCH_NOT_FOUND));
                branches.add(branch);
            }
        }

        // Create user account
        UserAccount user = new UserAccount();
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setStatus("active");
        user.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        user.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        UserAccount savedUser = userAccountRepository.save(user);
        log.info("User created successfully with ID: {}", savedUser.getId());

        // Assign roles
        for (Role role : roles) {
            UserRole userRole = new UserRole();
            userRole.setId(new UserRoleId(savedUser.getId(), role.getId()));
            userRole.setUser(savedUser);
            userRole.setRole(role);
            userRoleRepository.save(userRole);
        }
        log.info("Assigned {} roles to user {}", roles.size(), savedUser.getId());

        // Assign branches
        for (Branch branch : branches) {
            UserBranch userBranch = new UserBranch();
            userBranch.setId(new UserBranchId(savedUser.getId(), branch.getId()));
            userBranch.setUser(savedUser);
            userBranch.setBranch(branch);
            userBranch.setAssignedAt(OffsetDateTime.now(ZoneOffset.UTC));
            userBranchRepository.save(userBranch);
        }
        log.info("Assigned {} branches to user {}", branches.size(), savedUser.getId());

        return convertToDTO(savedUser);
    }

    @Override
    public UserDTO updateUser(Long id, UpdateUserRequestDTO request) {
        log.info("Updating user ID: {}", id);

        UserAccount user = userAccountRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // Update email if provided and different
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userAccountRepository.existsByEmail(request.getEmail())) {
                throw new CustomException(ErrorCode.USER_EMAIL_ALREADY_EXISTS);
            }
            user.setEmail(request.getEmail());
        }

        // Update phone if provided and different
        if (request.getPhone() != null && !request.getPhone().equals(user.getPhone())) {
            if (request.getPhone().isBlank()) {
                user.setPhone(null);
            } else {
                if (userAccountRepository.existsByPhone(request.getPhone())) {
                    throw new CustomException(ErrorCode.USER_PHONE_ALREADY_EXISTS);
                }
                user.setPhone(request.getPhone());
            }
        }

        // Update other fields
        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
        }

        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            user.setStatus(request.getStatus());
        }

        // Update roles if provided
        if (request.getRoleIds() != null) {
            // Delete existing roles
            userRoleRepository.deleteByUserId(id);

            // Add new roles
            for (Long roleId : request.getRoleIds()) {
                Role role = roleRepository.findById(roleId)
                        .orElseThrow(() -> new CustomException(ErrorCode.ROLE_NOT_FOUND));
                UserRole userRole = new UserRole();
                userRole.setId(new UserRoleId(id, roleId));
                userRole.setUser(user);
                userRole.setRole(role);
                userRoleRepository.save(userRole);
            }
            log.info("Updated roles for user {}", id);
        }

        // Update branches if provided
        if (request.getBranchIds() != null) {
            // Delete existing branches
            userBranchRepository.deleteByUserId(id);

            // Add new branches
            for (Long branchId : request.getBranchIds()) {
                Branch branch = branchRepository.findById(branchId)
                        .orElseThrow(() -> new CustomException(ErrorCode.BRANCH_NOT_FOUND));
                UserBranch userBranch = new UserBranch();
                userBranch.setId(new UserBranchId(id, branchId));
                userBranch.setUser(user);
                userBranch.setBranch(branch);
                userBranch.setAssignedAt(OffsetDateTime.now(ZoneOffset.UTC));
                userBranchRepository.save(userBranch);
            }
            log.info("Updated branches for user {}", id);
        }

        user.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        UserAccount updatedUser = userAccountRepository.save(user);

        log.info("User updated successfully: {}", updatedUser.getId());
        return convertToDTO(updatedUser);
    }

    @Override
    public void deleteUser(Long id) {
        log.info("Deleting user ID: {}", id);

        UserAccount user = userAccountRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // Soft delete - set status to inactive
        user.setStatus("inactive");
        user.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        userAccountRepository.save(user);

        log.info("User soft deleted (status set to inactive): {}", id);
    }

    @Override
    public void changePassword(Long userId, ChangePasswordRequestDTO request) {
        log.info("Changing password for user ID: {}", userId);

        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // Verify old password
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new CustomException(ErrorCode.PASSWORD_MISMATCH);
        }

        // Set new password
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        userAccountRepository.save(user);

        log.info("Password changed successfully for user: {}", userId);
    }

    @Override
    public void activateUser(Long id) {
        log.info("Activating user ID: {}", id);

        UserAccount user = userAccountRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        user.setStatus("active");
        user.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        userAccountRepository.save(user);

        log.info("User activated: {}", id);
    }

    @Override
    public void deactivateUser(Long id) {
        log.info("Deactivating user ID: {}", id);

        UserAccount user = userAccountRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        user.setStatus("inactive");
        user.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        userAccountRepository.save(user);

        log.info("User deactivated: {}", id);
    }

    // ============ Private Helper Methods ============

    /**
     * Convert UserAccount entity to UserDTO
     */
    private UserDTO convertToDTO(UserAccount user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setPhone(user.getPhone());
        dto.setStatus(user.getStatus());
        dto.setLastLoginAt(user.getLastLoginAt());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());

        // Fetch and convert roles
        List<UserRole> userRoles = userRoleRepository.findByUserId(user.getId());
        List<RoleDTO> roleDTOs = userRoles.stream()
                .map(ur -> {
                    RoleDTO roleDTO = new RoleDTO();
                    roleDTO.setId(ur.getRole().getId());
                    roleDTO.setCode(ur.getRole().getCode());
                    roleDTO.setName(ur.getRole().getName());
                    return roleDTO;
                })
                .collect(Collectors.toList());
        dto.setRoles(roleDTOs);

        // Fetch and convert branches
        List<UserBranch> userBranches = userBranchRepository.findByUserId(user.getId());
        List<BranchDTO> branchDTOs = userBranches.stream()
                .map(ub -> {
                    BranchDTO branchDTO = new BranchDTO();
                    branchDTO.setId(ub.getBranch().getId());
                    branchDTO.setCode(ub.getBranch().getCode());
                    branchDTO.setName(ub.getBranch().getName());
                    branchDTO.setAddress(ub.getBranch().getAddress());
                    branchDTO.setLocation(ub.getBranch().getLocation());
                    return branchDTO;
                })
                .collect(Collectors.toList());
        dto.setBranches(branchDTOs);

        return dto;
    }
}
