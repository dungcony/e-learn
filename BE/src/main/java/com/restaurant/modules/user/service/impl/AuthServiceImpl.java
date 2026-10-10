package com.restaurant.modules.user.service.impl;

import com.restaurant.common.exception.BusinessException;
import com.restaurant.common.exception.ErrorCode;
import com.restaurant.common.security.JwtService;
import com.restaurant.modules.user.config.AuthProperties;
import com.restaurant.modules.user.dto.request.EmailResendRequest;
import com.restaurant.modules.user.dto.request.PasswordForgotRequest;
import com.restaurant.modules.user.dto.request.PasswordResetRequest;
import com.restaurant.modules.user.dto.request.UserLoginRequest;
import com.restaurant.modules.user.dto.request.UserRegisterRequest;
import com.restaurant.modules.user.dto.response.AuthResponse;
import com.restaurant.modules.user.dto.response.ProfileResponse;
import com.restaurant.modules.user.entity.User;
import com.restaurant.modules.user.entity.UserToken;
import com.restaurant.modules.user.enums.Role;
import com.restaurant.modules.user.enums.UserStatus;
import com.restaurant.modules.user.enums.UserTokenType;
import com.restaurant.modules.user.events.PasswordResetRequestedEvent;
import com.restaurant.modules.user.events.UserRegisteredEvent;
import com.restaurant.modules.user.helper.UserTokenHelper;
import com.restaurant.modules.user.mapper.UserMapper;
import com.restaurant.modules.user.repository.UserRepository;
import com.restaurant.modules.user.repository.UserTokenRepository;
import com.restaurant.modules.user.service.AuthService;
import com.restaurant.modules.user.validator.UserValidator;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Luồng xác thực. Liên kết email được gửi qua sự kiện sau khi commit, nên hàm ở đây không gọi I/O ngoài trong transaction.
 * Đăng nhập luôn so mật khẩu (kể cả khi không thấy email) để thời gian phản hồi không lộ email có tồn tại hay không.
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserTokenRepository userTokenRepository;
    private final UserValidator userValidator;
    private final UserMapper userMapper;
    private final UserTokenHelper userTokenHelper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ApplicationEventPublisher eventPublisher;
    private final AuthProperties properties;
    private final Clock clock;

    // Băm của một mật khẩu ngẫu nhiên không ai biết, dùng để so khi không tìm thấy email
    private final String dummyPasswordHash;

    public AuthServiceImpl(UserRepository userRepository,
                           UserTokenRepository userTokenRepository,
                           UserValidator userValidator,
                           UserMapper userMapper,
                           UserTokenHelper userTokenHelper,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService,
                           ApplicationEventPublisher eventPublisher,
                           AuthProperties properties,
                           Clock clock) {
        this.userRepository = userRepository;
        this.userTokenRepository = userTokenRepository;
        this.userValidator = userValidator;
        this.userMapper = userMapper;
        this.userTokenHelper = userTokenHelper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.eventPublisher = eventPublisher;
        this.properties = properties;
        this.clock = clock;
        this.dummyPasswordHash = passwordEncoder.encode(UUID.randomUUID().toString());
    }

    @Override
    @Transactional
    public ProfileResponse register(UserRegisterRequest request) {
        String email = UserValidator.normalizeEmail(request.email());
        userValidator.validateEmailNotTaken(email, null);
        userValidator.validatePasswordConfirmed(request.password(), request.confirmPassword());

        User user = userMapper.fromRegisterRequest(request);
        user.setId(UUID.randomUUID());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(Role.CUSTOMER);
        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerified(false);
        user.setMustChangePassword(false);
        try {
            userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            // Hai request đăng ký cùng email chạy song song: unique index uq_users_email chặn request thứ hai
            throw new BusinessException(ErrorCode.AUTH_EMAIL_ALREADY_EXISTS);
        }

        String rawToken = createToken(user.getId(), UserTokenType.EMAIL_VERIFICATION, properties.verifyTokenTtl());
        eventPublisher.publishEvent(new UserRegisteredEvent(email, rawToken));
        return userMapper.toProfileResponse(user);
    }

    @Override
    @Transactional
    public void verifyEmail(String rawToken) {
        UserToken token = findUsableToken(rawToken, UserTokenType.EMAIL_VERIFICATION);
        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_CODE_INVALID));
        user.setEmailVerified(true);
        token.setUsedAt(clock.instant());
    }

    @Override
    @Transactional
    public void resendVerification(EmailResendRequest request) {
        User user = findByEmailOrThrow(request.email());
        if (user.isEmailVerified()) {
            throw new BusinessException(ErrorCode.AUTH_ACCOUNT_ALREADY_VERIFIED);
        }
        String rawToken = replaceToken(user.getId(), UserTokenType.EMAIL_VERIFICATION, properties.verifyTokenTtl());
        eventPublisher.publishEvent(new UserRegisteredEvent(user.getEmail(), rawToken));
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(UserLoginRequest request) {
        User user = userRepository.findByEmail(UserValidator.normalizeEmail(request.email())).orElse(null);
        String hashToCompare = user != null ? user.getPasswordHash() : dummyPasswordHash;
        boolean passwordMatches = passwordEncoder.matches(request.password(), hashToCompare);
        if (user == null || !passwordMatches) {
            throw new BusinessException(ErrorCode.AUTH_CREDENTIALS_INVALID);
        }
        // Kiểm tra trạng thái sau khi đã đúng mật khẩu để người lạ không dò được trạng thái tài khoản
        if (user.getStatus() == UserStatus.LOCKED) {
            throw new BusinessException(ErrorCode.AUTH_ACCOUNT_BLOCKED);
        }
        if (!user.isEmailVerified()) {
            throw new BusinessException(ErrorCode.AUTH_ACCOUNT_NOT_VERIFIED);
        }
        String accessToken = jwtService.generateAccessToken(user.getId(), List.of("ROLE_" + user.getRole().name()));
        return new AuthResponse(accessToken, jwtService.getAccessTokenExpirySeconds(), userMapper.toAuthUserResponse(user));
    }

    @Override
    @Transactional
    public void forgotPassword(PasswordForgotRequest request) {
        User user = findByEmailOrThrow(request.email());
        String rawToken = replaceToken(user.getId(), UserTokenType.PASSWORD_RESET, properties.resetTokenTtl());
        eventPublisher.publishEvent(new PasswordResetRequestedEvent(user.getEmail(), rawToken));
    }

    @Override
    @Transactional
    public void resetPassword(PasswordResetRequest request) {
        userValidator.validatePasswordConfirmed(request.newPassword(), request.confirmNewPassword());
        UserToken token = findUsableToken(request.token(), UserTokenType.PASSWORD_RESET);
        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_CODE_INVALID));
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setMustChangePassword(false);
        // Mở được liên kết gửi tới email chứng tỏ người đó sở hữu email
        user.setEmailVerified(true);
        token.setUsedAt(clock.instant());
    }

    private User findByEmailOrThrow(String email) {
        return userRepository.findByEmail(UserValidator.normalizeEmail(email))
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy tài khoản ứng với email này."));
    }

    private UserToken findUsableToken(String rawToken, UserTokenType type) {
        return userTokenRepository.findByTokenHash(userTokenHelper.hash(rawToken))
                .filter(token -> token.getType() == type && token.isUsableAt(clock.instant()))
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_CODE_INVALID));
    }

    // Vô hiệu token cũ còn hiệu lực, dọn token quá hạn rồi phát token mới
    private String replaceToken(UUID userId, UserTokenType type, Duration ttl) {
        Instant now = clock.instant();
        userTokenRepository.invalidateActiveTokens(userId, type, now);
        userTokenRepository.deleteExpired(userId, type, now);
        return createToken(userId, type, ttl);
    }

    private String createToken(UUID userId, UserTokenType type, Duration ttl) {
        UserTokenHelper.GeneratedToken generated = userTokenHelper.generate();
        UserToken token = new UserToken();
        token.setId(UUID.randomUUID());
        token.setUserId(userId);
        token.setType(type);
        token.setTokenHash(generated.hash());
        token.setExpiresAt(clock.instant().plus(ttl));
        userTokenRepository.save(token);
        return generated.raw();
    }
}
