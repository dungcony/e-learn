package com.elearning.user.service.impl;

import com.elearning.common.exception.BusinessException;
import com.elearning.common.mail.EmailService;
import com.elearning.common.security.JwtService;
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
import com.elearning.user.validator.UserValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordResetTokenRepository tokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private EmailService emailService;

    private AuthServiceImpl service;

    @BeforeEach
    void setUp() {
        UserMapper mapper = Mappers.getMapper(UserMapper.class);
        AuthProperties properties = new AuthProperties("http://localhost:3000/", Duration.ofMinutes(60), null, null);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        service = new AuthServiceImpl(userRepository, tokenRepository, passwordEncoder, jwtService, emailService,
                new UserValidator(userRepository), mapper, properties);
    }

    // ---- registerStudent ----

    @Test
    void registerStudent_createsActiveStudentWithLowercasedEmail() {
        when(userRepository.existsByEmail("student@example.com")).thenReturn(false);

        ProfileResponse response = service.registerStudent(
                new UserRegisterRequest("  Student@Example.COM ", "123456", "123456"));

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).saveAndFlush(saved.capture());
        assertThat(saved.getValue().getEmail()).isEqualTo("student@example.com");
        assertThat(saved.getValue().getRole()).isEqualTo(Role.STUDENT);
        assertThat(saved.getValue().getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(saved.getValue().getPasswordHash()).isEqualTo("hashed");
        assertThat(saved.getValue().getId()).isNotNull();
        assertThat(response.role()).isEqualTo(Role.STUDENT);
        assertThat(response.email()).isEqualTo("student@example.com");
    }

    @Test
    void registerStudent_confirmMismatchIsRejectedBeforeAnyWrite() {
        assertThatThrownBy(() -> service.registerStudent(new UserRegisterRequest("a@b.c", "123456", "654321")))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getCode()).isEqualTo("AUTH_PASSWORD_CONFIRM_MISMATCH"));
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void registerStudent_existingEmailConflicts() {
        when(userRepository.existsByEmail("a@b.c")).thenReturn(true);

        assertThatThrownBy(() -> service.registerStudent(new UserRegisterRequest("a@b.c", "123456", "123456")))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getCode()).isEqualTo("AUTH_EMAIL_ALREADY_EXISTS"));
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void registerStudent_uniqueIndexRaceMapsToEmailConflict() {
        when(userRepository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("uq_users_email"));

        assertThatThrownBy(() -> service.registerStudent(new UserRegisterRequest("a@b.c", "123456", "123456")))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getCode()).isEqualTo("AUTH_EMAIL_ALREADY_EXISTS"));
    }

    // ---- login ----

    @Test
    void login_returnsTokenWithRoleAuthority() {
        User user = user(Role.TEACHER, UserStatus.ACTIVE);
        when(userRepository.findByEmail("a@b.c")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("123456", "stored-hash")).thenReturn(true);
        when(jwtService.generateAccessToken(user.getId(), List.of("ROLE_TEACHER"))).thenReturn("jwt-token");

        AuthResponse response = service.login(new UserLoginRequest(" A@B.c ", "123456"));

        assertThat(response).isEqualTo(new AuthResponse("jwt-token", user.getId(), "a@b.c", Role.TEACHER));
    }

    @Test
    void login_unknownEmailStillRunsPasswordCheckAndReturnsSameError() {
        when(userRepository.findByEmail("nobody@x.y")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login(new UserLoginRequest("nobody@x.y", "123456")))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getCode()).isEqualTo("AUTH_CREDENTIALS_INVALID"));
        // So khớp với mật khẩu giả để thời gian phản hồi không lộ email chưa đăng ký.
        verify(passwordEncoder).matches(eq("123456"), eq("hashed"));
        verify(jwtService, never()).generateAccessToken(any(), any(java.util.Collection.class));
    }

    @Test
    void login_wrongPasswordIsRejected() {
        when(userRepository.findByEmail("a@b.c")).thenReturn(Optional.of(user(Role.STUDENT, UserStatus.ACTIVE)));
        when(passwordEncoder.matches("wrong1", "stored-hash")).thenReturn(false);

        assertThatThrownBy(() -> service.login(new UserLoginRequest("a@b.c", "wrong1")))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getCode()).isEqualTo("AUTH_CREDENTIALS_INVALID"));
    }

    @Test
    void login_lockedAccountWithRightPasswordIsBlocked() {
        when(userRepository.findByEmail("a@b.c")).thenReturn(Optional.of(user(Role.STUDENT, UserStatus.LOCKED)));
        when(passwordEncoder.matches("123456", "stored-hash")).thenReturn(true);

        assertThatThrownBy(() -> service.login(new UserLoginRequest("a@b.c", "123456")))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getCode()).isEqualTo("AUTH_ACCOUNT_BLOCKED"));
    }

    @Test
    void login_lockedAccountWithWrongPasswordDoesNotRevealLock() {
        when(userRepository.findByEmail("a@b.c")).thenReturn(Optional.of(user(Role.STUDENT, UserStatus.LOCKED)));
        when(passwordEncoder.matches("wrong1", "stored-hash")).thenReturn(false);

        assertThatThrownBy(() -> service.login(new UserLoginRequest("a@b.c", "wrong1")))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getCode()).isEqualTo("AUTH_CREDENTIALS_INVALID"));
    }

    // ---- requestPasswordReset ----

    @Test
    void requestPasswordReset_unknownEmailIsNotFoundAndSendsNothing() {
        when(userRepository.findByEmail("nobody@x.y")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.requestPasswordReset(new PasswordForgotRequest("nobody@x.y")))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getCode()).isEqualTo("NOT_FOUND"));
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
        verify(tokenRepository, never()).save(any());
    }

    @Test
    void requestPasswordReset_storesOnlyHashExpiresInSixtyMinutesAndEmailsLink() throws Exception {
        User user = user(Role.STUDENT, UserStatus.ACTIVE);
        when(userRepository.findByEmail("a@b.c")).thenReturn(Optional.of(user));
        Instant before = Instant.now();

        service.requestPasswordReset(new PasswordForgotRequest("a@b.c"));

        verify(tokenRepository).invalidateUnusedByUserId(eq(user.getId()), any(Instant.class));
        ArgumentCaptor<PasswordResetToken> token = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepository).save(token.capture());
        ArgumentCaptor<String> content = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendEmail(eq("a@b.c"), anyString(), content.capture());

        String rawToken = content.getValue().lines()
                .filter(l -> l.startsWith("http://localhost:3000/reset-password?token="))
                .findFirst().orElseThrow().substring("http://localhost:3000/reset-password?token=".length());
        assertThat(rawToken).hasSizeGreaterThanOrEqualTo(40);
        assertThat(token.getValue().getTokenHash()).isNotEqualTo(rawToken).isEqualTo(sha256(rawToken));
        assertThat(token.getValue().getUserId()).isEqualTo(user.getId());
        assertThat(token.getValue().getExpiresAt())
                .isBetween(before.plus(Duration.ofMinutes(60)), Instant.now().plus(Duration.ofMinutes(60)));
        assertThat(token.getValue().getUsedAt()).isNull();
    }

    // ---- resetPassword ----

    @Test
    void resetPassword_validTokenChangesPasswordAndMarksTokenUsed() {
        User user = user(Role.STUDENT, UserStatus.ACTIVE);
        PasswordResetToken token = token(user.getId(), Instant.now().plusSeconds(600), null);
        when(tokenRepository.findByTokenHash(sha256("raw-token"))).thenReturn(Optional.of(token));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        service.resetPassword(new PasswordResetRequest("raw-token", "abcdef", "abcdef"));

        assertThat(user.getPasswordHash()).isEqualTo("hashed");
        assertThat(token.getUsedAt()).isNotNull();
    }

    @Test
    void resetPassword_confirmMismatchFailsBeforeTouchingToken() {
        assertThatThrownBy(() -> service.resetPassword(new PasswordResetRequest("raw-token", "abcdef", "abcxyz")))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getCode()).isEqualTo("AUTH_PASSWORD_CONFIRM_MISMATCH"));
        verify(tokenRepository, never()).findByTokenHash(anyString());
    }

    @Test
    void resetPassword_unknownExpiredOrUsedTokenAllFailTheSameWay() {
        UUID userId = UUID.randomUUID();
        when(tokenRepository.findByTokenHash(sha256("unknown"))).thenReturn(Optional.empty());
        when(tokenRepository.findByTokenHash(sha256("expired")))
                .thenReturn(Optional.of(token(userId, Instant.now().minusSeconds(1), null)));
        when(tokenRepository.findByTokenHash(sha256("used")))
                .thenReturn(Optional.of(token(userId, Instant.now().plusSeconds(600), Instant.now())));

        for (String raw : List.of("unknown", "expired", "used")) {
            assertThatThrownBy(() -> service.resetPassword(new PasswordResetRequest(raw, "abcdef", "abcdef")))
                    .as(raw)
                    .isInstanceOfSatisfying(BusinessException.class,
                            e -> assertThat(e.getCode()).isEqualTo("AUTH_CODE_INVALID"));
        }
        verify(userRepository, never()).findById(any());
    }

    @Test
    void resetPassword_deletedAccountIsInvalidToken() {
        UUID userId = UUID.randomUUID();
        when(tokenRepository.findByTokenHash(sha256("raw")))
                .thenReturn(Optional.of(token(userId, Instant.now().plusSeconds(600), null)));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.resetPassword(new PasswordResetRequest("raw", "abcdef", "abcdef")))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getCode()).isEqualTo("AUTH_CODE_INVALID"));
    }

    private User user(Role role, UserStatus status) {
        return User.builder().id(UUID.randomUUID()).email("a@b.c").passwordHash("stored-hash")
                .role(role).status(status).build();
    }

    private PasswordResetToken token(UUID userId, Instant expiresAt, Instant usedAt) {
        return PasswordResetToken.builder().id(UUID.randomUUID()).userId(userId).tokenHash("x")
                .expiresAt(expiresAt).usedAt(usedAt).build();
    }

    private String sha256(String value) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
