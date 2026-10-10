package com.restaurant.modules.user.service.impl;

import com.restaurant.common.exception.BusinessException;
import com.restaurant.common.response.PageRequestParams;
import com.restaurant.common.security.BlacklistedUserRepository;
import com.restaurant.common.security.JwtService;
import com.restaurant.common.storage.FileStorageService;
import com.restaurant.modules.user.UserTestSupport;
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
import com.restaurant.modules.user.enums.Gender;
import com.restaurant.modules.user.enums.Role;
import com.restaurant.modules.user.enums.UserGroup;
import com.restaurant.modules.user.enums.UserStatus;
import com.restaurant.modules.user.mapper.UserMapperImpl;
import com.restaurant.modules.user.repository.UserBriefProjection;
import com.restaurant.modules.user.repository.UserRepository;
import com.restaurant.modules.user.service.UserDeletionGuard;
import com.restaurant.modules.user.validator.UserValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    private static final Instant NOW = Instant.parse("2026-10-09T02:00:00Z");

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private FileStorageService fileStorageService;
    @Mock
    private BlacklistedUserRepository blacklistedUserRepository;
    @Mock
    private JwtService jwtService;

    private UserServiceImpl service;
    private User user;

    @BeforeEach
    void setUp() {
        service = buildService();
        user = newUser(Role.WAITER);
        user.setMustChangePassword(true);
    }

    private UserServiceImpl buildService(UserDeletionGuard... guards) {
        return new UserServiceImpl(userRepository, new UserValidator(userRepository, UserTestSupport.guards(guards)),
                new UserMapperImpl(), passwordEncoder, fileStorageService, blacklistedUserRepository, jwtService,
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    private User newUser(Role role) {
        User u = new User();
        u.setId(UUID.randomUUID());
        u.setEmail("an@gmail.com");
        u.setPasswordHash("stored-hash");
        u.setFullName("Nguyễn Văn An");
        u.setRole(role);
        u.setStatus(UserStatus.ACTIVE);
        u.setEmailVerified(true);
        return u;
    }

    // ---- hồ sơ của chính mình (UC003, UC005) ----

    @Test
    void getProfile_returns_own_profile() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        ProfileResponse response = service.getProfile(user.getId());

        assertThat(response.id()).isEqualTo(user.getId());
        assertThat(response.email()).isEqualTo("an@gmail.com");
        assertThat(response.mustChangePassword()).isTrue();
    }

    @Test
    void getProfile_unknown_user_is_not_found() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getProfile(user.getId()))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("NOT_FOUND");
    }

    @Test
    void updateProfile_changes_only_profile_fields_and_normalizes_email() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.saveAndFlush(user)).thenReturn(user);

        ProfileResponse response = service.updateProfile(user.getId(), new ProfileUpdateRequest(
                "Trần Thị Bích", " Bich@Nhahang.vn ", LocalDate.of(1995, 5, 20), "0912345678", Gender.FEMALE));

        assertThat(user.getFullName()).isEqualTo("Trần Thị Bích");
        assertThat(user.getEmail()).isEqualTo("bich@nhahang.vn");
        assertThat(user.getPhone()).isEqualTo("0912345678");
        assertThat(user.getGender()).isEqualTo(Gender.FEMALE);
        assertThat(user.getRole()).isEqualTo(Role.WAITER);
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(response.email()).isEqualTo("bich@nhahang.vn");
    }

    @Test
    void updateProfile_rejects_email_of_another_account() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailAndIdNot("khac@gmail.com", user.getId())).thenReturn(true);

        assertThatThrownBy(() -> service.updateProfile(user.getId(),
                new ProfileUpdateRequest("An", "khac@gmail.com", null, null, null)))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("AUTH_EMAIL_ALREADY_EXISTS");
    }

    @Test
    void updateAvatar_stores_image_and_saves_url() {
        MockMultipartFile file = new MockMultipartFile("file", "a.png", "image/png", new byte[]{1});
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(fileStorageService.storeImage(file, "avatars")).thenReturn("/files/avatars/x.png");
        when(userRepository.saveAndFlush(user)).thenReturn(user);

        ProfileResponse response = service.updateAvatar(user.getId(), file);

        assertThat(user.getAvatarUrl()).isEqualTo("/files/avatars/x.png");
        assertThat(response.avatarUrl()).isEqualTo("/files/avatars/x.png");
    }

    @Test
    void changePassword_sets_new_hash_and_clears_first_login_flag() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Matkhau123", "stored-hash")).thenReturn(true);
        when(passwordEncoder.matches("Matkhau456", "stored-hash")).thenReturn(false);
        when(passwordEncoder.encode("Matkhau456")).thenReturn("new-hash");

        service.changePassword(user.getId(), new PasswordChangeRequest("Matkhau123", "Matkhau456", "Matkhau456"));

        assertThat(user.getPasswordHash()).isEqualTo("new-hash");
        assertThat(user.isMustChangePassword()).isFalse();
        verify(userRepository).save(user);
    }

    @Test
    void changePassword_rejects_wrong_old_password() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("sai", "stored-hash")).thenReturn(false);

        assertThatThrownBy(() -> service.changePassword(user.getId(),
                new PasswordChangeRequest("sai", "Matkhau456", "Matkhau456")))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("AUTH_OLD_PASSWORD_INCORRECT");
    }

    @Test
    void changePassword_rejects_new_password_equal_to_old() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Matkhau123", "stored-hash")).thenReturn(true);

        assertThatThrownBy(() -> service.changePassword(user.getId(),
                new PasswordChangeRequest("Matkhau123", "Matkhau123", "Matkhau123")))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("AUTH_PASSWORD_SAME_AS_OLD");
    }

    @Test
    void changePassword_rejects_mismatched_confirmation() {
        assertThatThrownBy(() -> service.changePassword(user.getId(),
                new PasswordChangeRequest("Matkhau123", "Matkhau456", "Matkhau000")))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("AUTH_PASSWORD_CONFIRM_MISMATCH");
        verify(userRepository, never()).save(any());
    }

    // ---- tìm kiếm (UC006) ----

    @SuppressWarnings("unchecked")
    @Test
    void searchUsers_returns_summaries_with_default_sort_by_created_at_desc() {
        when(userRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(user)));

        Page<UserSummaryResponse> page = service.searchUsers(UserGroup.STAFF,
                new UserSearchRequest("an", null, null, Role.WAITER, UserStatus.ACTIVE, null),
                PageRequestParams.of(null, null, null, null));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).role()).isEqualTo(Role.WAITER);
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(userRepository).findAll(any(Specification.class), pageable.capture());
        assertThat(pageable.getValue().getPageNumber()).isZero();
        assertThat(pageable.getValue().getPageSize()).isEqualTo(20);
        assertThat(pageable.getValue().getSort().getOrderFor("createdAt")).isNotNull();
    }

    @Test
    void searchUsers_rejects_customer_role_in_staff_group() {
        assertThatThrownBy(() -> service.searchUsers(UserGroup.STAFF,
                new UserSearchRequest(null, null, null, Role.CUSTOMER, null, null),
                PageRequestParams.of(null, null, null, null)))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("VALIDATION_ERROR");
    }

    // ---- xem chi tiết ----

    @Test
    void getUser_returns_detail_of_the_requested_group() {
        when(userRepository.findByIdAndRoleIn(user.getId(), Role.STAFF_ROLES)).thenReturn(Optional.of(user));

        UserDetailResponse response = service.getUser(UserGroup.STAFF, user.getId());

        assertThat(response.id()).isEqualTo(user.getId());
        assertThat(response.mustChangePassword()).isTrue();
    }

    @Test
    void getUser_of_the_other_group_is_not_found() {
        UUID id = UUID.randomUUID();
        when(userRepository.findByIdAndRoleIn(id, Set.of(Role.CUSTOMER))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getUser(UserGroup.CUSTOMER, id))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("NOT_FOUND");
    }

    // ---- thêm / sửa nhân viên (UC008) ----

    @Test
    void createStaff_creates_verified_account_that_must_change_password() {
        when(passwordEncoder.encode("Matkhau123")).thenReturn("hashed");
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserDetailResponse response = service.createStaff(new StaffCreateRequest("Trần Thị Bích", " Bich@Nhahang.vn ",
                Role.CASHIER, "Matkhau123", UserStatus.ACTIVE, LocalDate.of(1995, 5, 20), "0912345678", Gender.FEMALE));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).saveAndFlush(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getEmail()).isEqualTo("bich@nhahang.vn");
        assertThat(saved.getPasswordHash()).isEqualTo("hashed");
        assertThat(saved.getRole()).isEqualTo(Role.CASHIER);
        assertThat(saved.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(saved.isEmailVerified()).isTrue();
        assertThat(saved.isMustChangePassword()).isTrue();
        assertThat(response.role()).isEqualTo(Role.CASHIER);
    }

    @Test
    void createStaff_rejects_customer_role() {
        assertThatThrownBy(() -> service.createStaff(new StaffCreateRequest("An", "an@gmail.com", Role.CUSTOMER,
                "Matkhau123", UserStatus.ACTIVE, null, null, null)))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("VALIDATION_ERROR");
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void createStaff_rejects_taken_email() {
        when(userRepository.existsByEmail("an@gmail.com")).thenReturn(true);

        assertThatThrownBy(() -> service.createStaff(new StaffCreateRequest("An", "an@gmail.com", Role.WAITER,
                "Matkhau123", UserStatus.ACTIVE, null, null, null)))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("AUTH_EMAIL_ALREADY_EXISTS");
    }

    @Test
    void updateStaff_changes_role_of_another_staff() {
        UUID manager = UUID.randomUUID();
        when(userRepository.findByIdAndRoleIn(user.getId(), Role.STAFF_ROLES)).thenReturn(Optional.of(user));
        when(userRepository.saveAndFlush(user)).thenReturn(user);

        UserDetailResponse response = service.updateStaff(manager, user.getId(), new StaffUpdateRequest(
                "Nguyễn Văn An", "an@gmail.com", Role.CASHIER, null, "0912345678", null));

        assertThat(user.getRole()).isEqualTo(Role.CASHIER);
        assertThat(user.getPhone()).isEqualTo("0912345678");
        assertThat(response.role()).isEqualTo(Role.CASHIER);
    }

    @Test
    void updateStaff_rejects_manager_changing_own_role() {
        User self = newUser(Role.MANAGER);
        when(userRepository.findByIdAndRoleIn(self.getId(), Role.STAFF_ROLES)).thenReturn(Optional.of(self));

        assertThatThrownBy(() -> service.updateStaff(self.getId(), self.getId(), new StaffUpdateRequest(
                "An", "an@gmail.com", Role.WAITER, null, null, null)))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("USER_CANNOT_MODIFY_SELF");
        assertThat(self.getRole()).isEqualTo(Role.MANAGER);
    }

    @Test
    void updateStaff_allows_manager_editing_own_other_fields() {
        User self = newUser(Role.MANAGER);
        when(userRepository.findByIdAndRoleIn(self.getId(), Role.STAFF_ROLES)).thenReturn(Optional.of(self));
        when(userRepository.saveAndFlush(self)).thenReturn(self);

        service.updateStaff(self.getId(), self.getId(), new StaffUpdateRequest(
                "Tên mới", "an@gmail.com", Role.MANAGER, null, null, null));

        assertThat(self.getFullName()).isEqualTo("Tên mới");
    }

    @Test
    void updateStaffAvatar_stores_image() {
        MockMultipartFile file = new MockMultipartFile("file", "a.png", "image/png", new byte[]{1});
        when(userRepository.findByIdAndRoleIn(user.getId(), Role.STAFF_ROLES)).thenReturn(Optional.of(user));
        when(fileStorageService.storeImage(file, "avatars")).thenReturn("/files/avatars/x.png");
        when(userRepository.saveAndFlush(user)).thenReturn(user);

        UserDetailResponse response = service.updateStaffAvatar(user.getId(), file);

        assertThat(response.avatarUrl()).isEqualTo("/files/avatars/x.png");
    }

    // ---- khóa / mở khóa (UC008, UC009) ----

    @Test
    void changeStatus_lock_blacklists_active_tokens_for_one_access_token_lifetime() {
        UUID manager = UUID.randomUUID();
        when(userRepository.findByIdAndRoleIn(user.getId(), Role.STAFF_ROLES)).thenReturn(Optional.of(user));
        when(userRepository.saveAndFlush(user)).thenReturn(user);
        when(jwtService.getAccessTokenExpirySeconds()).thenReturn(3600L);

        UserDetailResponse response = service.changeStatus(UserGroup.STAFF, manager, user.getId(), UserStatus.LOCKED);

        assertThat(response.status()).isEqualTo(UserStatus.LOCKED);
        verify(blacklistedUserRepository).add(user.getId(), "LOCKED", 3600L);
    }

    @Test
    void changeStatus_unlock_removes_blacklist_entry() {
        user.setStatus(UserStatus.LOCKED);
        when(userRepository.findByIdAndRoleIn(user.getId(), Role.STAFF_ROLES)).thenReturn(Optional.of(user));
        when(userRepository.saveAndFlush(user)).thenReturn(user);

        service.changeStatus(UserGroup.STAFF, UUID.randomUUID(), user.getId(), UserStatus.ACTIVE);

        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        verify(blacklistedUserRepository).deleteById(user.getId().toString());
    }

    @Test
    void changeStatus_rejects_manager_locking_self() {
        UUID self = UUID.randomUUID();

        assertThatThrownBy(() -> service.changeStatus(UserGroup.STAFF, self, self, UserStatus.LOCKED))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("USER_CANNOT_MODIFY_SELF");
        verify(blacklistedUserRepository, never()).add(any(), any(), org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    void changeStatus_unknown_account_is_not_found() {
        UUID id = UUID.randomUUID();
        when(userRepository.findByIdAndRoleIn(id, Set.of(Role.CUSTOMER))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.changeStatus(UserGroup.CUSTOMER, UUID.randomUUID(), id, UserStatus.LOCKED))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("NOT_FOUND");
    }

    // ---- xóa (UC008, UC009) ----

    @Test
    void deleteUser_soft_deletes_and_blacklists() {
        when(userRepository.findByIdAndRoleIn(user.getId(), Role.STAFF_ROLES)).thenReturn(Optional.of(user));
        when(jwtService.getAccessTokenExpirySeconds()).thenReturn(3600L);

        service.deleteUser(UserGroup.STAFF, UUID.randomUUID(), user.getId());

        assertThat(user.getDeletedAt()).isEqualTo(NOW);
        verify(userRepository).save(user);
        verify(blacklistedUserRepository).add(user.getId(), "DELETED", 3600L);
    }

    @Test
    void deleteUser_rejects_when_a_guard_reports_activity() {
        UserServiceImpl guarded = buildService(id -> true);
        when(userRepository.findByIdAndRoleIn(user.getId(), Role.STAFF_ROLES)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> guarded.deleteUser(UserGroup.STAFF, UUID.randomUUID(), user.getId()))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("USER_HAS_ACTIVITY");
        assertThat(user.getDeletedAt()).isNull();
        verify(blacklistedUserRepository, never()).add(any(), any(), org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    void deleteUser_rejects_manager_deleting_self() {
        UUID self = UUID.randomUUID();

        assertThatThrownBy(() -> service.deleteUser(UserGroup.STAFF, self, self))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("USER_CANNOT_MODIFY_SELF");
    }

    @Test
    void deleteUser_unknown_account_is_not_found() {
        UUID id = UUID.randomUUID();
        when(userRepository.findByIdAndRoleIn(id, Set.of(Role.CUSTOMER))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteUser(UserGroup.CUSTOMER, UUID.randomUUID(), id))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("NOT_FOUND");
    }

    // ---- API công khai cho module khác ----

    @Test
    void getUserBriefs_with_no_ids_does_not_query() {
        assertThat(service.getUserBriefs(List.of())).isEmpty();
        verify(userRepository, never()).findBriefsIncludingDeleted(anyCollection());
    }

    @Test
    void getUserBriefs_maps_rows_including_deleted_accounts() {
        UUID id = UUID.randomUUID();
        UserBriefProjection row = new UserBriefProjection() {
            public UUID getId() { return id; }
            public String getFullName() { return "Trần Thị Bích"; }
            public String getEmail() { return "bich@nhahang.vn"; }
            public String getPhone() { return "0912345678"; }
            public String getRole() { return "CASHIER"; }
        };
        Collection<UUID> ids = List.of(id);
        when(userRepository.findBriefsIncludingDeleted(ids)).thenReturn(List.of(row));

        List<UserBriefResponse> briefs = service.getUserBriefs(ids);

        assertThat(briefs).singleElement().satisfies(b -> {
            assertThat(b.id()).isEqualTo(id);
            assertThat(b.fullName()).isEqualTo("Trần Thị Bích");
            assertThat(b.role()).isEqualTo(Role.CASHIER);
        });
    }
}
