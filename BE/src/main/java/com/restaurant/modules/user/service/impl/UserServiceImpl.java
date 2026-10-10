package com.restaurant.modules.user.service.impl;

import com.restaurant.common.exception.BusinessException;
import com.restaurant.common.exception.ErrorCode;
import com.restaurant.common.response.PageRequestParams;
import com.restaurant.common.security.BlacklistedUserRepository;
import com.restaurant.common.security.JwtService;
import com.restaurant.common.storage.FileStorageService;
import com.restaurant.common.util.SpecificationUtils;
import com.restaurant.modules.user.dto.request.PasswordChangeRequest;
import com.restaurant.modules.user.dto.request.ProfileUpdateRequest;
import com.restaurant.modules.user.dto.request.StaffCreateRequest;
import com.restaurant.modules.user.dto.request.StaffUpdateRequest;
import com.restaurant.modules.user.dto.request.UserSearchRequest;
import com.restaurant.modules.user.dto.response.ProfileResponse;
import com.restaurant.modules.user.dto.response.UserBriefResponse;
import com.restaurant.modules.user.dto.response.UserDetailResponse;
import com.restaurant.modules.user.dto.response.UserSummaryResponse;
import com.restaurant.modules.user.entity.User;
import com.restaurant.modules.user.enums.Role;
import com.restaurant.modules.user.enums.UserGroup;
import com.restaurant.modules.user.enums.UserStatus;
import com.restaurant.modules.user.mapper.UserMapper;
import com.restaurant.modules.user.repository.UserRepository;
import com.restaurant.modules.user.service.UserService;
import com.restaurant.modules.user.validator.UserValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Clock;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final String AVATAR_DIRECTORY = "avatars";

    // Tên field client được phép sắp xếp (snake_case) -> thuộc tính entity
    private static final Map<String, String> SORTABLE_FIELDS =
            Map.of("created_at", "createdAt", "full_name", "fullName", "email", "email");

    private final UserRepository userRepository;
    private final UserValidator userValidator;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;
    private final BlacklistedUserRepository blacklistedUserRepository;
    private final JwtService jwtService;
    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getProfile(UUID userId) {
        return userMapper.toProfileResponse(loadUser(userId));
    }

    @Override
    @Transactional
    public ProfileResponse updateProfile(UUID userId, ProfileUpdateRequest request) {
        User user = loadUser(userId);
        String email = UserValidator.normalizeEmail(request.email());
        userValidator.validateEmailNotTaken(email, userId);

        userMapper.updateFromRequest(request, user);
        user.setEmail(email);
        return userMapper.toProfileResponse(saveAndFlushUnique(user));
    }

    @Override
    @Transactional
    public ProfileResponse updateAvatar(UUID userId, MultipartFile file) {
        User user = loadUser(userId);
        user.setAvatarUrl(fileStorageService.storeImage(file, AVATAR_DIRECTORY));
        return userMapper.toProfileResponse(saveAndFlushUnique(user));
    }

    @Override
    @Transactional
    public void changePassword(UUID userId, PasswordChangeRequest request) {
        userValidator.validatePasswordConfirmed(request.newPassword(), request.confirmNewPassword());
        User user = loadUser(userId);
        if (!passwordEncoder.matches(request.oldPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.AUTH_OLD_PASSWORD_INCORRECT);
        }
        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.AUTH_PASSWORD_SAME_AS_OLD);
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setMustChangePassword(false);
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserSummaryResponse> searchUsers(UserGroup group, UserSearchRequest request, PageRequestParams params) {
        if (group == UserGroup.STAFF && request.role() != null) {
            userValidator.validateStaffRole(request.role());
        }
        Specification<User> spec = (root, query, cb) -> root.get("role").in(group.roles());
        spec = spec.and(SpecificationUtils.containsIgnoreCase("fullName", request.name()))
                .and(SpecificationUtils.containsIgnoreCase("email", request.email()))
                .and(SpecificationUtils.containsIgnoreCase("phone", request.phone()));
        if (group == UserGroup.STAFF && request.role() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("role"), request.role()));
        }
        if (request.status() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), request.status()));
        }
        if (group == UserGroup.CUSTOMER && request.emailVerified() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("emailVerified"), request.emailVerified()));
        }
        return userRepository.findAll(spec, params.toPageable(SORTABLE_FIELDS, "createdAt"))
                .map(userMapper::toSummaryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetailResponse getUser(UserGroup group, UUID id) {
        return userMapper.toDetailResponse(loadInGroup(group, id));
    }

    @Override
    @Transactional
    public UserDetailResponse createStaff(StaffCreateRequest request) {
        userValidator.validateStaffRole(request.role());
        String email = UserValidator.normalizeEmail(request.email());
        userValidator.validateEmailNotTaken(email, null);

        User user = userMapper.fromStaffCreateRequest(request);
        user.setId(UUID.randomUUID());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        // Quản lý đã biết email của nhân viên nên không cần xác thực; mật khẩu do Quản lý đặt nên phải đổi ở lần đầu
        user.setEmailVerified(true);
        user.setMustChangePassword(true);
        return userMapper.toDetailResponse(saveAndFlushUnique(user));
    }

    @Override
    @Transactional
    public UserDetailResponse updateStaff(UUID currentUserId, UUID id, StaffUpdateRequest request) {
        User user = loadInGroup(UserGroup.STAFF, id);
        userValidator.validateStaffRole(request.role());
        if (id.equals(currentUserId) && request.role() != user.getRole()) {
            userValidator.validateNotSelf(currentUserId, id);
        }
        String email = UserValidator.normalizeEmail(request.email());
        userValidator.validateEmailNotTaken(email, id);

        userMapper.updateStaffFromRequest(request, user);
        user.setEmail(email);
        return userMapper.toDetailResponse(saveAndFlushUnique(user));
    }

    @Override
    @Transactional
    public UserDetailResponse updateStaffAvatar(UUID id, MultipartFile file) {
        User user = loadInGroup(UserGroup.STAFF, id);
        user.setAvatarUrl(fileStorageService.storeImage(file, AVATAR_DIRECTORY));
        return userMapper.toDetailResponse(saveAndFlushUnique(user));
    }

    @Override
    @Transactional
    public UserDetailResponse changeStatus(UserGroup group, UUID currentUserId, UUID id, UserStatus status) {
        userValidator.validateNotSelf(currentUserId, id);
        User user = loadInGroup(group, id);
        user.setStatus(status);
        User saved = saveAndFlushUnique(user);
        // Redis nằm ngoài transaction JPA nên ghi sau khi lưu thành công
        if (status == UserStatus.LOCKED) {
            blacklistedUserRepository.add(id, "LOCKED", jwtService.getAccessTokenExpirySeconds());
        } else {
            blacklistedUserRepository.deleteById(id.toString());
        }
        return userMapper.toDetailResponse(saved);
    }

    @Override
    @Transactional
    public void deleteUser(UserGroup group, UUID currentUserId, UUID id) {
        userValidator.validateNotSelf(currentUserId, id);
        User user = loadInGroup(group, id);
        userValidator.validateDeletable(id);

        user.setDeletedAt(clock.instant());
        userRepository.save(user);
        blacklistedUserRepository.add(id, "DELETED", jwtService.getAccessTokenExpirySeconds());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserBriefResponse> getUserBriefs(Collection<UUID> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return userRepository.findBriefsIncludingDeleted(ids).stream()
                .map(row -> new UserBriefResponse(row.getId(), row.getFullName(), row.getEmail(), row.getPhone(),
                        Role.valueOf(row.getRole())))
                .toList();
    }

    private User loadUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy tài khoản."));
    }

    // Tra theo nhóm để id của nhóm kia (nhân viên với khách hàng) cũng trả NOT_FOUND
    private User loadInGroup(UserGroup group, UUID id) {
        return userRepository.findByIdAndRoleIn(id, group.roles())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy tài khoản."));
    }

    // Flush để createdAt/updatedAt có giá trị khi trả response; unique index bắt trường hợp hai request trùng email song song
    private User saveAndFlushUnique(User user) {
        try {
            return userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.AUTH_EMAIL_ALREADY_EXISTS);
        }
    }
}
