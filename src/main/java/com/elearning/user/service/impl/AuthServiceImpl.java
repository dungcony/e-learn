package com.elearning.user.service.impl;

import com.elearning.common.exception.BusinessException;
import com.elearning.common.exception.ErrorCode;
import com.elearning.common.mail.EmailService;
import com.elearning.common.security.JwtService;
import com.elearning.common.util.TransactionUtils;
import com.elearning.user.config.AuthProperties;
import com.elearning.user.dto.request.PasswordForgotRequest;
import com.elearning.user.dto.request.PasswordResetRequest;
import com.elearning.user.dto.request.UserLoginRequest;
import com.elearning.user.dto.request.UserRegisterRequest;
import com.elearning.user.dto.response.AuthResponse;
import com.elearning.user.dto.response.ProfileResponse;
import com.elearning.user.entity.PasswordResetToken;
import com.elearning.user.entity.User;
import com.elearning.user.enums.Role;
import com.elearning.user.enums.UserStatus;
import com.elearning.user.mapper.UserMapper;
import com.elearning.user.repository.PasswordResetTokenRepository;
import com.elearning.user.repository.UserRepository;
import com.elearning.user.service.AuthService;
import com.elearning.user.validator.UserValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    private static final int RESET_TOKEN_BYTES = 32;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final UserValidator userValidator;
    private final UserMapper userMapper;
    private final AuthProperties authProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    // Băm một mật khẩu giả để đăng nhập email không tồn tại tốn thời gian ngang đăng nhập sai mật khẩu,
    // nếu không đo thời gian phản hồi sẽ biết email nào đã đăng ký.
    private final String dummyPasswordHash;

    public AuthServiceImpl(UserRepository userRepository, PasswordResetTokenRepository tokenRepository,
                           PasswordEncoder passwordEncoder, JwtService jwtService, EmailService emailService,
                           UserValidator userValidator, UserMapper userMapper, AuthProperties authProperties) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.emailService = emailService;
        this.userValidator = userValidator;
        this.userMapper = userMapper;
        this.authProperties = authProperties;
        this.dummyPasswordHash = passwordEncoder.encode(UUID.randomUUID().toString());
    }

    @Override
    @Transactional
    public ProfileResponse registerStudent(UserRegisterRequest request) {
        String email = normalizeEmail(request.email());
        userValidator.validatePasswordConfirm(request.password(), request.confirmPassword());
        userValidator.validateEmailNotTaken(email, null);
        User user = User.builder()
                .id(UUID.randomUUID())
                .email(email)
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(Role.STUDENT)
                .status(UserStatus.ACTIVE)
                .build();
        try {
            userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            // Hai request cùng email chạy song song cùng qua bước kiểm tra, unique index chặn người đến sau.
            throw new BusinessException(ErrorCode.AUTH_EMAIL_ALREADY_EXISTS);
        }
        return userMapper.toProfileResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(UserLoginRequest request) {
        User user = userRepository.findByEmail(normalizeEmail(request.email())).orElse(null);
        String hashToCheck = user == null ? dummyPasswordHash : user.getPasswordHash();
        boolean passwordMatches = passwordEncoder.matches(request.password(), hashToCheck);
        if (user == null || !passwordMatches) {
            throw new BusinessException(ErrorCode.AUTH_CREDENTIALS_INVALID);
        }
        if (user.getStatus() == UserStatus.LOCKED) {
            throw new BusinessException(ErrorCode.AUTH_ACCOUNT_BLOCKED);
        }
        String token = jwtService.generateAccessToken(user.getId(), List.of("ROLE_" + user.getRole().name()));
        return new AuthResponse(token, user.getId(), user.getEmail(), user.getRole());
    }

    @Override
    @Transactional
    public void requestPasswordReset(PasswordForgotRequest request) {
        User user = userRepository.findByEmail(normalizeEmail(request.email()))
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy tài khoản với email này."));
        Instant now = Instant.now();
        tokenRepository.invalidateUnusedByUserId(user.getId(), now);

        String rawToken = generateToken();
        tokenRepository.save(PasswordResetToken.builder()
                .id(UUID.randomUUID())
                .userId(user.getId())
                .tokenHash(hash(rawToken))
                .expiresAt(now.plus(authProperties.resetTokenTtl()))
                .build());

        String link = trimTrailingSlash(authProperties.frontendUrl()) + "/reset-password?token=" + rawToken;
        String content = """
                Bạn vừa yêu cầu đặt lại mật khẩu tài khoản E-Learning.

                Bấm vào liên kết sau để đặt mật khẩu mới (liên kết có hiệu lực %d phút và chỉ dùng được một lần):
                %s

                Nếu không phải bạn yêu cầu thì bỏ qua email này, mật khẩu hiện tại vẫn giữ nguyên.
                """.formatted(authProperties.resetTokenTtl().toMinutes(), link);
        TransactionUtils.runAfterCommit(() -> emailService.sendEmail(user.getEmail(), "Đặt lại mật khẩu E-Learning", content));
    }

    @Override
    @Transactional
    public void resetPassword(PasswordResetRequest request) {
        userValidator.validatePasswordConfirm(request.newPassword(), request.confirmPassword());
        PasswordResetToken token = tokenRepository.findByTokenHash(hash(request.token()))
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_CODE_INVALID));
        Instant now = Instant.now();
        if (token.getUsedAt() != null || token.getExpiresAt().isBefore(now)) {
            throw new BusinessException(ErrorCode.AUTH_CODE_INVALID);
        }
        // Tài khoản đã bị xóa mềm thì findById không thấy.
        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_CODE_INVALID));
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        token.setUsedAt(now);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String generateToken() {
        byte[] bytes = new byte[RESET_TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 bắt buộc có trong mọi JVM.
            throw new IllegalStateException("Thiếu SHA-256", e);
        }
    }

    private String trimTrailingSlash(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
