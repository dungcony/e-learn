package com.restaurant.modules.user.service.impl;

import com.restaurant.common.exception.BusinessException;
import com.restaurant.common.security.JwtService;
import com.restaurant.modules.user.UserTestSupport;
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
import com.restaurant.modules.user.mapper.UserMapperImpl;
import com.restaurant.modules.user.repository.UserRepository;
import com.restaurant.modules.user.repository.UserTokenRepository;
import com.restaurant.modules.user.validator.UserValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    private static final Instant NOW = Instant.parse("2026-10-09T02:00:00Z");

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserTokenRepository userTokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private final UserTokenHelper tokenHelper = new UserTokenHelper();
    private final UserMapper userMapper = new UserMapperImpl();
    private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
    private final AuthProperties properties =
            new AuthProperties("http://localhost:3000", Duration.ofMinutes(60), Duration.ofHours(24), "", "");

    private AuthServiceImpl service;

    @BeforeEach
    void setUp() {
        lenient().when(passwordEncoder.encode(anyString())).thenReturn("dummy-hash");
        service = new AuthServiceImpl(userRepository, userTokenRepository, new UserValidator(userRepository, UserTestSupport.guards()),
                userMapper, tokenHelper, passwordEncoder, jwtService, eventPublisher, properties, clock);
    }

    // ---- register ----

    @Test
    void register_creates_unverified_customer_and_sends_verification_token() {
        when(passwordEncoder.encode("Matkhau123")).thenReturn("hashed");
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        ProfileResponse response = service.register(new UserRegisterRequest(
                "Nguyễn Văn An", " An.Nguyen@Gmail.com ", "0989123456", "Matkhau123", "Matkhau123"));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).saveAndFlush(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertThat(saved.getEmail()).isEqualTo("an.nguyen@gmail.com");
        assertThat(saved.getPasswordHash()).isEqualTo("hashed");
        assertThat(saved.getRole()).isEqualTo(Role.CUSTOMER);
        assertThat(saved.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(saved.isEmailVerified()).isFalse();
        assertThat(saved.isMustChangePassword()).isFalse();
        assertThat(response.role()).isEqualTo(Role.CUSTOMER);

        ArgumentCaptor<UserToken> tokenCaptor = ArgumentCaptor.forClass(UserToken.class);
        verify(userTokenRepository).save(tokenCaptor.capture());
        UserToken token = tokenCaptor.getValue();
        assertThat(token.getType()).isEqualTo(UserTokenType.EMAIL_VERIFICATION);
        assertThat(token.getUserId()).isEqualTo(saved.getId());
        assertThat(token.getExpiresAt()).isEqualTo(NOW.plus(Duration.ofHours(24)));

        ArgumentCaptor<UserRegisteredEvent> eventCaptor = ArgumentCaptor.forClass(UserRegisteredEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().email()).isEqualTo("an.nguyen@gmail.com");
        assertThat(tokenHelper.hash(eventCaptor.getValue().rawToken())).isEqualTo(token.getTokenHash());
    }

    @Test
    void register_rejects_taken_email() {
        when(userRepository.existsByEmail("an@gmail.com")).thenReturn(true);

        assertThatThrownBy(() -> service.register(registerRequest("Matkhau123", "Matkhau123")))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("AUTH_EMAIL_ALREADY_EXISTS");
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void register_rejects_mismatched_confirmation() {
        assertThatThrownBy(() -> service.register(registerRequest("Matkhau123", "Matkhau999")))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("AUTH_PASSWORD_CONFIRM_MISMATCH");
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void register_maps_unique_index_violation_to_email_exists() {
        when(userRepository.saveAndFlush(any(User.class))).thenThrow(new DataIntegrityViolationException("uq_users_email"));

        assertThatThrownBy(() -> service.register(registerRequest("Matkhau123", "Matkhau123")))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("AUTH_EMAIL_ALREADY_EXISTS");
        verify(userTokenRepository, never()).save(any());
    }

    // ---- verifyEmail ----

    @Test
    void verifyEmail_marks_user_verified_and_token_used() {
        User user = user(Role.CUSTOMER, false);
        UserToken token = token(user.getId(), UserTokenType.EMAIL_VERIFICATION, NOW.plusSeconds(60), null);
        when(userTokenRepository.findByTokenHash(tokenHelper.hash("raw"))).thenReturn(Optional.of(token));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        service.verifyEmail("raw");

        assertThat(user.isEmailVerified()).isTrue();
        assertThat(token.getUsedAt()).isEqualTo(NOW);
    }

    @Test
    void verifyEmail_rejects_unknown_expired_used_and_wrong_type_tokens() {
        UUID userId = UUID.randomUUID();
        when(userTokenRepository.findByTokenHash(tokenHelper.hash("unknown"))).thenReturn(Optional.empty());
        when(userTokenRepository.findByTokenHash(tokenHelper.hash("expired"))).thenReturn(Optional.of(
                token(userId, UserTokenType.EMAIL_VERIFICATION, NOW.minusSeconds(1), null)));
        when(userTokenRepository.findByTokenHash(tokenHelper.hash("used"))).thenReturn(Optional.of(
                token(userId, UserTokenType.EMAIL_VERIFICATION, NOW.plusSeconds(60), NOW.minusSeconds(5))));
        when(userTokenRepository.findByTokenHash(tokenHelper.hash("reset"))).thenReturn(Optional.of(
                token(userId, UserTokenType.PASSWORD_RESET, NOW.plusSeconds(60), null)));

        for (String raw : List.of("unknown", "expired", "used", "reset")) {
            assertThatThrownBy(() -> service.verifyEmail(raw))
                    .as(raw)
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo("AUTH_CODE_INVALID");
        }
    }

    // ---- resendVerification ----

    @Test
    void resendVerification_replaces_previous_token_and_publishes_event() {
        User user = user(Role.CUSTOMER, false);
        when(userRepository.findByEmail("an@gmail.com")).thenReturn(Optional.of(user));

        service.resendVerification(new EmailResendRequest(" AN@gmail.com "));

        verify(userTokenRepository).invalidateActiveTokens(user.getId(), UserTokenType.EMAIL_VERIFICATION, NOW);
        verify(userTokenRepository).save(any(UserToken.class));
        verify(eventPublisher).publishEvent(any(UserRegisteredEvent.class));
    }

    @Test
    void resendVerification_rejects_unknown_email() {
        assertThatThrownBy(() -> service.resendVerification(new EmailResendRequest("none@gmail.com")))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("NOT_FOUND");
    }

    @Test
    void resendVerification_rejects_already_verified_account() {
        when(userRepository.findByEmail("an@gmail.com")).thenReturn(Optional.of(user(Role.CUSTOMER, true)));

        assertThatThrownBy(() -> service.resendVerification(new EmailResendRequest("an@gmail.com")))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("AUTH_ACCOUNT_ALREADY_VERIFIED");
    }

    // ---- login ----

    @Test
    void login_returns_token_and_role_for_valid_credentials() {
        User user = user(Role.CASHIER, true);
        user.setMustChangePassword(true);
        when(userRepository.findByEmail("an@gmail.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Matkhau123", "stored-hash")).thenReturn(true);
        when(jwtService.generateAccessToken(user.getId(), List.of("ROLE_CASHIER"))).thenReturn("jwt");
        when(jwtService.getAccessTokenExpirySeconds()).thenReturn(3600L);

        AuthResponse response = service.login(new UserLoginRequest("An@gmail.com", "Matkhau123"));

        assertThat(response.accessToken()).isEqualTo("jwt");
        assertThat(response.expiresIn()).isEqualTo(3600L);
        assertThat(response.user().role()).isEqualTo(Role.CASHIER);
        assertThat(response.user().mustChangePassword()).isTrue();
    }

    @Test
    void login_rejects_wrong_password() {
        when(userRepository.findByEmail("an@gmail.com")).thenReturn(Optional.of(user(Role.WAITER, true)));
        when(passwordEncoder.matches("sai", "stored-hash")).thenReturn(false);

        assertThatThrownBy(() -> service.login(new UserLoginRequest("an@gmail.com", "sai")))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("AUTH_CREDENTIALS_INVALID");
    }

    @Test
    void login_unknown_email_still_compares_against_a_dummy_hash_and_gives_same_error() {
        when(passwordEncoder.matches("Matkhau123", "dummy-hash")).thenReturn(false);

        assertThatThrownBy(() -> service.login(new UserLoginRequest("none@gmail.com", "Matkhau123")))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("AUTH_CREDENTIALS_INVALID");
        verify(passwordEncoder).matches("Matkhau123", "dummy-hash");
    }

    @Test
    void login_rejects_locked_account_only_after_password_is_correct() {
        User user = user(Role.WAITER, true);
        user.setStatus(UserStatus.LOCKED);
        when(userRepository.findByEmail("an@gmail.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Matkhau123", "stored-hash")).thenReturn(true);

        assertThatThrownBy(() -> service.login(new UserLoginRequest("an@gmail.com", "Matkhau123")))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("AUTH_ACCOUNT_BLOCKED");
    }

    @Test
    void login_rejects_unverified_customer() {
        when(userRepository.findByEmail("an@gmail.com")).thenReturn(Optional.of(user(Role.CUSTOMER, false)));
        when(passwordEncoder.matches("Matkhau123", "stored-hash")).thenReturn(true);

        assertThatThrownBy(() -> service.login(new UserLoginRequest("an@gmail.com", "Matkhau123")))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("AUTH_ACCOUNT_NOT_VERIFIED");
    }

    // ---- forgotPassword / resetPassword ----

    @Test
    void forgotPassword_invalidates_old_tokens_and_publishes_event_with_new_one() {
        User user = user(Role.WAITER, true);
        when(userRepository.findByEmail("an@gmail.com")).thenReturn(Optional.of(user));

        service.forgotPassword(new PasswordForgotRequest("an@gmail.com"));

        verify(userTokenRepository).invalidateActiveTokens(user.getId(), UserTokenType.PASSWORD_RESET, NOW);
        ArgumentCaptor<UserToken> tokenCaptor = ArgumentCaptor.forClass(UserToken.class);
        verify(userTokenRepository).save(tokenCaptor.capture());
        assertThat(tokenCaptor.getValue().getType()).isEqualTo(UserTokenType.PASSWORD_RESET);
        assertThat(tokenCaptor.getValue().getExpiresAt()).isEqualTo(NOW.plus(Duration.ofMinutes(60)));
        verify(eventPublisher).publishEvent(any(PasswordResetRequestedEvent.class));
    }

    @Test
    void forgotPassword_rejects_unknown_email() {
        assertThatThrownBy(() -> service.forgotPassword(new PasswordForgotRequest("none@gmail.com")))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("NOT_FOUND");
    }

    @Test
    void resetPassword_sets_new_hash_clears_flag_and_verifies_email() {
        User user = user(Role.CUSTOMER, false);
        user.setMustChangePassword(true);
        UserToken token = token(user.getId(), UserTokenType.PASSWORD_RESET, NOW.plusSeconds(60), null);
        when(userTokenRepository.findByTokenHash(tokenHelper.hash("raw"))).thenReturn(Optional.of(token));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("Matkhau789")).thenReturn("new-hash");

        service.resetPassword(new PasswordResetRequest("raw", "Matkhau789", "Matkhau789"));

        assertThat(user.getPasswordHash()).isEqualTo("new-hash");
        assertThat(user.isMustChangePassword()).isFalse();
        assertThat(user.isEmailVerified()).isTrue();
        assertThat(token.getUsedAt()).isEqualTo(NOW);
    }

    @Test
    void resetPassword_rejects_expired_token() {
        UserToken token = token(UUID.randomUUID(), UserTokenType.PASSWORD_RESET, NOW.minusSeconds(1), null);
        when(userTokenRepository.findByTokenHash(tokenHelper.hash("raw"))).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.resetPassword(new PasswordResetRequest("raw", "Matkhau789", "Matkhau789")))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("AUTH_CODE_INVALID");
    }

    @Test
    void resetPassword_rejects_mismatched_confirmation_before_touching_the_token() {
        assertThatThrownBy(() -> service.resetPassword(new PasswordResetRequest("raw", "Matkhau789", "Matkhau000")))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("AUTH_PASSWORD_CONFIRM_MISMATCH");
        verify(userTokenRepository, never()).findByTokenHash(any());
    }

    // ---- helpers ----

    private UserRegisterRequest registerRequest(String password, String confirm) {
        return new UserRegisterRequest("Nguyễn Văn An", "an@gmail.com", null, password, confirm);
    }

    private User user(Role role, boolean emailVerified) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("an@gmail.com");
        user.setPasswordHash("stored-hash");
        user.setFullName("Nguyễn Văn An");
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerified(emailVerified);
        return user;
    }

    private UserToken token(UUID userId, UserTokenType type, Instant expiresAt, Instant usedAt) {
        UserToken token = new UserToken();
        token.setId(UUID.randomUUID());
        token.setUserId(userId);
        token.setType(type);
        token.setExpiresAt(expiresAt);
        token.setUsedAt(usedAt);
        return token;
    }
}
