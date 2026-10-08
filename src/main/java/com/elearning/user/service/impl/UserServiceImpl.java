package com.elearning.user.service.impl;

import com.elearning.common.exception.BusinessException;
import com.elearning.common.exception.ErrorCode;
import com.elearning.common.response.PageRequestParams;
import com.elearning.common.security.BlacklistedUserRepository;
import com.elearning.common.security.JwtService;
import com.elearning.common.storage.FileStorageService;
import com.elearning.common.util.TransactionUtils;
import com.elearning.user.dto.request.PasswordChangeRequest;
import com.elearning.user.dto.request.ProfileUpdateRequest;
import com.elearning.user.dto.request.TeacherCreateRequest;
import com.elearning.user.dto.request.TeacherUpdateRequest;
import com.elearning.user.dto.request.UserSearchRequest;
import com.elearning.user.dto.response.ProfileResponse;
import com.elearning.user.dto.response.UserBriefResponse;
import com.elearning.user.dto.response.UserDetailResponse;
import com.elearning.user.dto.response.UserSummaryResponse;
import com.elearning.user.entity.User;
import com.elearning.user.enums.Role;
import com.elearning.user.enums.UserStatus;
import com.elearning.user.mapper.UserMapper;
import com.elearning.user.repository.UserRepository;
import com.elearning.user.repository.UserSpecification;
import com.elearning.user.service.UserService;
import com.elearning.user.validator.UserValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    // Tên field snake_case client được sắp xếp theo → thuộc tính entity.
    private static final Map<String, String> SORTABLE_FIELDS =
            Map.of("created_at", "createdAt", "full_name", "fullName", "email", "email");

    private static final String BLOCK_REASON_LOCKED = "Tài khoản bị khóa";
    private static final String BLOCK_REASON_DELETED = "Tài khoản đã bị xóa";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserValidator userValidator;
    private final UserMapper userMapper;
    private final BlacklistedUserRepository blacklistedUserRepository;
    private final JwtService jwtService;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getProfile(UUID userId) {
        return userMapper.toProfileResponse(findUser(userId));
    }

    @Override
    @Transactional
    public ProfileResponse updateProfile(UUID userId, ProfileUpdateRequest request) {
        User user = findUser(userId);
        String email = normalizeEmail(request.email());
        userValidator.validateEmailNotTaken(email, userId);

        if (request.password() != null || request.confirmPassword() != null) {
            if (user.getRole() == Role.STUDENT) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                        "Học viên đổi mật khẩu qua PUT /users/me/password.");
            }
            userValidator.validatePasswordConfirm(request.password(), request.confirmPassword());
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }
        userMapper.updateProfile(request, user);
        user.setEmail(email);
        return userMapper.toProfileResponse(user);
    }

    @Override
    @Transactional
    public ProfileResponse updateAvatar(UUID userId, MultipartFile file) {
        User user = findUser(userId);
        user.setAvatarUrl(fileStorageService.storeImage(file, "avatars"));
        return userMapper.toProfileResponse(user);
    }

    @Override
    @Transactional
    public void changePassword(UUID userId, PasswordChangeRequest request) {
        User user = findUser(userId);
        if (!passwordEncoder.matches(request.oldPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.AUTH_OLD_PASSWORD_INCORRECT);
        }
        userValidator.validatePasswordConfirm(request.newPassword(), request.confirmPassword());
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserSummaryResponse> searchUsers(Role role, UserSearchRequest request, PageRequestParams params) {
        return userRepository
                .findAll(UserSpecification.search(role, request), params.toPageable(SORTABLE_FIELDS, "createdAt"))
                .map(userMapper::toSummaryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetailResponse getUser(UUID id, Role role) {
        return userMapper.toDetailResponse(findByIdAndRole(id, role));
    }

    @Override
    @Transactional
    public UserDetailResponse createTeacher(TeacherCreateRequest request) {
        String email = normalizeEmail(request.email());
        userValidator.validateEmailNotTaken(email, null);
        User teacher = userMapper.toNewTeacher(request);
        teacher.setId(UUID.randomUUID());
        teacher.setEmail(email);
        teacher.setPasswordHash(passwordEncoder.encode(request.password()));
        teacher.setRole(Role.TEACHER);
        return userMapper.toDetailResponse(userRepository.save(teacher));
    }

    @Override
    @Transactional
    public UserDetailResponse updateTeacher(UUID id, TeacherUpdateRequest request) {
        User teacher = findByIdAndRole(id, Role.TEACHER);
        String email = normalizeEmail(request.email());
        userValidator.validateEmailNotTaken(email, id);

        userMapper.updateTeacher(request, teacher);
        teacher.setEmail(email);
        if (request.password() != null) {
            teacher.setPasswordHash(passwordEncoder.encode(request.password()));
        }
        applyStatus(teacher, request.status());
        return userMapper.toDetailResponse(teacher);
    }

    @Override
    @Transactional
    public UserDetailResponse updateTeacherAvatar(UUID id, MultipartFile file) {
        User teacher = findByIdAndRole(id, Role.TEACHER);
        teacher.setAvatarUrl(fileStorageService.storeImage(file, "avatars"));
        return userMapper.toDetailResponse(teacher);
    }

    @Override
    @Transactional
    public UserDetailResponse changeStudentStatus(UUID id, UserStatus status) {
        User student = findByIdAndRole(id, Role.STUDENT);
        applyStatus(student, status);
        return userMapper.toDetailResponse(student);
    }

    @Override
    @Transactional
    public void deleteUser(UUID id, Role role) {
        User user = findByIdAndRole(id, role);
        user.setDeletedAt(Instant.now());
        blockTokens(user.getId(), BLOCK_REASON_DELETED);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserBriefResponse> getUserBriefs(Collection<UUID> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return userRepository.findBriefsByIds(ids).stream().map(userMapper::toBriefResponse).toList();
    }

    // Đổi trạng thái và đồng bộ danh sách chặn token; không làm gì nếu trạng thái không đổi.
    private void applyStatus(User user, UserStatus newStatus) {
        if (user.getStatus() == newStatus) {
            return;
        }
        user.setStatus(newStatus);
        if (newStatus == UserStatus.LOCKED) {
            blockTokens(user.getId(), BLOCK_REASON_LOCKED);
        } else {
            UUID userId = user.getId();
            TransactionUtils.runAfterCommit(() -> blacklistedUserRepository.deleteById(userId.toString()));
        }
    }

    // TTL bằng thời hạn access token: hết TTL thì mọi token cũ cũng đã tự hết hạn.
    private void blockTokens(UUID userId, String reason) {
        long ttlSeconds = jwtService.getAccessTokenExpirySeconds();
        TransactionUtils.runAfterCommit(() -> blacklistedUserRepository.add(userId, reason, ttlSeconds));
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy người dùng."));
    }

    private User findByIdAndRole(UUID id, Role role) {
        return userRepository.findByIdAndRole(id, role)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy người dùng."));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
