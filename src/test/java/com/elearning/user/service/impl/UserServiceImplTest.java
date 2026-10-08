package com.elearning.user.service.impl;

import com.elearning.common.exception.BusinessException;
import com.elearning.common.response.PageRequestParams;
import com.elearning.common.security.BlacklistedUserRepository;
import com.elearning.common.security.JwtService;
import com.elearning.common.storage.FileStorageService;
import com.elearning.user.dto.request.PasswordChangeRequest;
import com.elearning.user.dto.request.ProfileUpdateRequest;
import com.elearning.user.dto.request.TeacherCreateRequest;
import com.elearning.user.dto.request.TeacherUpdateRequest;
import com.elearning.user.dto.request.UserSearchRequest;
import com.elearning.user.dto.response.ProfileResponse;
import com.elearning.user.dto.response.UserBriefResponse;
import com.elearning.user.dto.response.UserDetailResponse;
import com.elearning.user.entity.User;
import com.elearning.user.enums.Gender;
import com.elearning.user.enums.Role;
import com.elearning.user.enums.UserStatus;
import com.elearning.user.mapper.UserMapper;
import com.elearning.user.repository.UserBriefView;
import com.elearning.user.repository.UserRepository;
import com.elearning.user.validator.UserValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private BlacklistedUserRepository blacklistedUserRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private FileStorageService fileStorageService;

    private UserServiceImpl service;

    @BeforeEach
    void setUp() {
        UserMapper mapper = Mappers.getMapper(UserMapper.class);
        service = new UserServiceImpl(userRepository, passwordEncoder, new UserValidator(userRepository), mapper,
                blacklistedUserRepository, jwtService, fileStorageService);
    }

    // ---- profile ----

    @Test
    void updateProfile_studentCannotSendPassword() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.of(user(id, Role.STUDENT)));

        assertThatThrownBy(() -> service.updateProfile(id, profileRequest("a@b.c", "newpass", "newpass")))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getCode()).isEqualTo("VALIDATION_ERROR"));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void updateProfile_teacherPasswordIsHashedWhenConfirmed() {
        UUID id = UUID.randomUUID();
        User teacher = user(id, Role.TEACHER);
        when(userRepository.findById(id)).thenReturn(Optional.of(teacher));
        when(passwordEncoder.encode("newpass")).thenReturn("new-hash");

        service.updateProfile(id, profileRequest("a@b.c", "newpass", "newpass"));

        assertThat(teacher.getPasswordHash()).isEqualTo("new-hash");
    }

    @Test
    void updateProfile_confirmWithoutPasswordIsMismatch() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.of(user(id, Role.ADMIN)));

        assertThatThrownBy(() -> service.updateProfile(id, profileRequest("a@b.c", null, "newpass")))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getCode()).isEqualTo("AUTH_PASSWORD_CONFIRM_MISMATCH"));
    }

    @Test
    void updateProfile_overwritesFieldsAndLowercasesEmailButKeepsRoleAndPassword() {
        UUID id = UUID.randomUUID();
        User student = user(id, Role.STUDENT);
        when(userRepository.findById(id)).thenReturn(Optional.of(student));

        ProfileResponse response = service.updateProfile(id, new ProfileUpdateRequest(
                "Nguyễn Văn A", "NEW@Example.com", LocalDate.of(1996, 4, 15), "0989123456", Gender.MALE, null, null));

        assertThat(student.getEmail()).isEqualTo("new@example.com");
        assertThat(student.getFullName()).isEqualTo("Nguyễn Văn A");
        assertThat(student.getPhone()).isEqualTo("0989123456");
        assertThat(student.getRole()).isEqualTo(Role.STUDENT);
        assertThat(student.getPasswordHash()).isEqualTo("stored-hash");
        assertThat(response.email()).isEqualTo("new@example.com");
    }

    @Test
    void updateProfile_emailOfAnotherAccountConflicts() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.of(user(id, Role.STUDENT)));
        when(userRepository.existsByEmailAndIdNot("taken@x.y", id)).thenReturn(true);

        assertThatThrownBy(() -> service.updateProfile(id, profileRequest("taken@x.y", null, null)))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getCode()).isEqualTo("AUTH_EMAIL_ALREADY_EXISTS"));
    }

    @Test
    void updateAvatar_storesUnderAvatarsAndSavesUrl() {
        UUID id = UUID.randomUUID();
        User user = user(id, Role.STUDENT);
        MockMultipartFile file = new MockMultipartFile("file", "a.png", "image/png", new byte[]{1});
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(fileStorageService.storeImage(file, "avatars")).thenReturn("/files/avatars/x.png");

        ProfileResponse response = service.updateAvatar(id, file);

        assertThat(user.getAvatarUrl()).isEqualTo("/files/avatars/x.png");
        assertThat(response.avatarUrl()).isEqualTo("/files/avatars/x.png");
    }

    @Test
    void changePassword_wrongOldPasswordIsRejected() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.of(user(id, Role.STUDENT)));
        when(passwordEncoder.matches("nope", "stored-hash")).thenReturn(false);

        assertThatThrownBy(() -> service.changePassword(id, new PasswordChangeRequest("nope", "newpass", "newpass")))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getCode()).isEqualTo("AUTH_OLD_PASSWORD_INCORRECT"));
    }

    @Test
    void changePassword_confirmMismatchIsRejectedEvenWithRightOldPassword() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.of(user(id, Role.STUDENT)));
        when(passwordEncoder.matches("abcdef", "stored-hash")).thenReturn(true);

        assertThatThrownBy(() -> service.changePassword(id, new PasswordChangeRequest("abcdef", "newpass", "other1")))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getCode()).isEqualTo("AUTH_PASSWORD_CONFIRM_MISMATCH"));
    }

    @Test
    void changePassword_success() {
        UUID id = UUID.randomUUID();
        User user = user(id, Role.STUDENT);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("abcdef", "stored-hash")).thenReturn(true);
        when(passwordEncoder.encode("newpass")).thenReturn("new-hash");

        service.changePassword(id, new PasswordChangeRequest("abcdef", "newpass", "newpass"));

        assertThat(user.getPasswordHash()).isEqualTo("new-hash");
    }

    // ---- admin: search ----

    @Test
    @SuppressWarnings("unchecked")
    void searchUsers_mapsSnakeCaseSortAndFallsBackForUnknownField() {
        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());

        service.searchUsers(Role.TEACHER, new UserSearchRequest(null, null, null, null),
                PageRequestParams.of(2, 5, "full_name", "asc"));
        service.searchUsers(Role.TEACHER, new UserSearchRequest(null, null, null, null),
                PageRequestParams.of(1, 20, "password_hash", "asc"));

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(userRepository, org.mockito.Mockito.times(2)).findAll(any(Specification.class), pageable.capture());
        Pageable first = pageable.getAllValues().get(0);
        assertThat(first.getPageNumber()).isEqualTo(1);
        assertThat(first.getSort().getOrderFor("fullName").getDirection()).isEqualTo(Sort.Direction.ASC);
        Pageable second = pageable.getAllValues().get(1);
        assertThat(second.getSort().getOrderFor("createdAt")).isNotNull();
        assertThat(second.getSort().getOrderFor("passwordHash")).isNull();
    }

    @Test
    void getUser_idOfAnotherRoleIsNotFound() {
        UUID id = UUID.randomUUID();
        when(userRepository.findByIdAndRole(id, Role.TEACHER)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getUser(id, Role.TEACHER))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getCode()).isEqualTo("NOT_FOUND"));
    }

    // ---- admin: teachers ----

    @Test
    void createTeacher_forcesTeacherRoleLowercasesEmailAndHashesPassword() {
        when(passwordEncoder.encode("teach123")).thenReturn("teacher-hash");
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserDetailResponse response = service.createTeacher(new TeacherCreateRequest(
                "Giảng Viên", "Teacher@Example.com", "teach123", UserStatus.ACTIVE, null, "0911222333", Gender.FEMALE));

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).saveAndFlush(saved.capture());
        assertThat(saved.getValue().getRole()).isEqualTo(Role.TEACHER);
        assertThat(saved.getValue().getEmail()).isEqualTo("teacher@example.com");
        assertThat(saved.getValue().getPasswordHash()).isEqualTo("teacher-hash");
        assertThat(saved.getValue().getId()).isNotNull();
        assertThat(response.role()).isEqualTo(Role.TEACHER);
    }

    @Test
    void createTeacher_duplicateEmailConflicts() {
        when(userRepository.existsByEmail("t@x.y")).thenReturn(true);

        assertThatThrownBy(() -> service.createTeacher(
                new TeacherCreateRequest("GV", "t@x.y", "teach123", UserStatus.ACTIVE, null, null, null)))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getCode()).isEqualTo("AUTH_EMAIL_ALREADY_EXISTS"));
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void updateTeacher_withoutPasswordKeepsOldHashAndReplacesOptionalFields() {
        UUID id = UUID.randomUUID();
        User teacher = user(id, Role.TEACHER);
        teacher.setPhone("0911222333");
        when(userRepository.findByIdAndRole(id, Role.TEACHER)).thenReturn(Optional.of(teacher));

        service.updateTeacher(id, new TeacherUpdateRequest("GV Mới", "t@x.y", null, UserStatus.ACTIVE, null, null, null));

        assertThat(teacher.getFullName()).isEqualTo("GV Mới");
        assertThat(teacher.getPhone()).isNull();
        assertThat(teacher.getPasswordHash()).isEqualTo("stored-hash");
        verifyNoInteractions(blacklistedUserRepository);
    }

    @Test
    void updateTeacher_lockingBlacklistsTokensForAccessTokenLifetime() {
        UUID id = UUID.randomUUID();
        User teacher = user(id, Role.TEACHER);
        when(userRepository.findByIdAndRole(id, Role.TEACHER)).thenReturn(Optional.of(teacher));
        when(jwtService.getAccessTokenExpirySeconds()).thenReturn(3600L);

        service.updateTeacher(id, new TeacherUpdateRequest("GV", "t@x.y", null, UserStatus.LOCKED, null, null, null));

        assertThat(teacher.getStatus()).isEqualTo(UserStatus.LOCKED);
        verify(blacklistedUserRepository).add(eq(id), anyString(), eq(3600L));
    }

    @Test
    void updateTeacher_unlockingRemovesBlacklistEntry() {
        UUID id = UUID.randomUUID();
        User teacher = user(id, Role.TEACHER);
        teacher.setStatus(UserStatus.LOCKED);
        when(userRepository.findByIdAndRole(id, Role.TEACHER)).thenReturn(Optional.of(teacher));

        service.updateTeacher(id, new TeacherUpdateRequest("GV", "t@x.y", null, UserStatus.ACTIVE, null, null, null));

        verify(blacklistedUserRepository).deleteById(id.toString());
        verify(blacklistedUserRepository, never()).add(any(), anyString(), anyLong());
    }

    // ---- admin: students, delete, public API ----

    @Test
    void changeStudentStatus_unknownStudentIsNotFound() {
        UUID id = UUID.randomUUID();
        when(userRepository.findByIdAndRole(id, Role.STUDENT)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.changeStudentStatus(id, UserStatus.LOCKED))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getCode()).isEqualTo("NOT_FOUND"));
        verifyNoInteractions(blacklistedUserRepository);
    }

    @Test
    void changeStudentStatus_sameStatusDoesNothing() {
        UUID id = UUID.randomUUID();
        when(userRepository.findByIdAndRole(id, Role.STUDENT)).thenReturn(Optional.of(user(id, Role.STUDENT)));

        service.changeStudentStatus(id, UserStatus.ACTIVE);

        verifyNoInteractions(blacklistedUserRepository);
    }

    @Test
    void deleteUser_softDeletesAndBlacklistsTokens() {
        UUID id = UUID.randomUUID();
        User student = user(id, Role.STUDENT);
        when(userRepository.findByIdAndRole(id, Role.STUDENT)).thenReturn(Optional.of(student));
        when(jwtService.getAccessTokenExpirySeconds()).thenReturn(3600L);

        service.deleteUser(id, Role.STUDENT);

        assertThat(student.getDeletedAt()).isNotNull();
        verify(userRepository, never()).delete(any(User.class));
        verify(blacklistedUserRepository).add(eq(id), anyString(), eq(3600L));
    }

    @Test
    void getUserBriefs_emptyInputSkipsQuery() {
        assertThat(service.getUserBriefs(List.of())).isEmpty();
        verifyNoInteractions(userRepository);
    }

    @Test
    void getUserBriefs_mapsProjectionRows() {
        UUID id = UUID.randomUUID();
        UserBriefView view = new UserBriefView() {
            public UUID getId() { return id; }
            public String getFullName() { return "Cũ Đã Xóa"; }
            public String getEmail() { return "old@x.y"; }
        };
        when(userRepository.findBriefsByIds(List.of(id))).thenReturn(List.of(view));

        assertThat(service.getUserBriefs(List.of(id)))
                .containsExactly(new UserBriefResponse(id, "Cũ Đã Xóa", "old@x.y"));
    }

    private User user(UUID id, Role role) {
        return User.builder().id(id).email("a@b.c").passwordHash("stored-hash")
                .role(role).status(UserStatus.ACTIVE).build();
    }

    private ProfileUpdateRequest profileRequest(String email, String password, String confirm) {
        return new ProfileUpdateRequest(null, email, null, null, null, password, confirm);
    }
}
