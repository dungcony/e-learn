package com.restaurant.modules.user.validator;

import com.restaurant.common.exception.BusinessException;
import com.restaurant.modules.user.UserTestSupport;
import com.restaurant.modules.user.enums.Role;
import com.restaurant.modules.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserValidatorTest {

    @Mock
    private UserRepository userRepository;

    private UserValidator validator;

    @BeforeEach
    void setUp() {
        validator = new UserValidator(userRepository, UserTestSupport.guards());
    }

    @Test
    void normalizeEmail_trims_and_lowercases() {
        assertThat(UserValidator.normalizeEmail("  An.Nguyen@Gmail.COM ")).isEqualTo("an.nguyen@gmail.com");
    }

    @Test
    void validateEmailNotTaken_passes_when_email_is_free() {
        when(userRepository.existsByEmail("a@x.com")).thenReturn(false);

        assertThatCode(() -> validator.validateEmailNotTaken("a@x.com", null)).doesNotThrowAnyException();
    }

    @Test
    void validateEmailNotTaken_rejects_taken_email_for_new_account() {
        when(userRepository.existsByEmail("a@x.com")).thenReturn(true);

        assertThatThrownBy(() -> validator.validateEmailNotTaken("a@x.com", null))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("AUTH_EMAIL_ALREADY_EXISTS");
    }

    @Test
    void validateEmailNotTaken_ignores_the_owner_when_updating() {
        UUID self = UUID.randomUUID();
        when(userRepository.existsByEmailAndIdNot("a@x.com", self)).thenReturn(false);

        assertThatCode(() -> validator.validateEmailNotTaken("a@x.com", self)).doesNotThrowAnyException();
    }

    @Test
    void validateEmailNotTaken_rejects_email_of_another_account_when_updating() {
        UUID self = UUID.randomUUID();
        when(userRepository.existsByEmailAndIdNot("a@x.com", self)).thenReturn(true);

        assertThatThrownBy(() -> validator.validateEmailNotTaken("a@x.com", self))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("AUTH_EMAIL_ALREADY_EXISTS");
    }

    @Test
    void validatePasswordConfirmed_rejects_mismatch() {
        assertThatThrownBy(() -> validator.validatePasswordConfirmed("Matkhau123", "Matkhau124"))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("AUTH_PASSWORD_CONFIRM_MISMATCH");
    }

    @Test
    void validatePasswordConfirmed_accepts_equal_passwords() {
        assertThatCode(() -> validator.validatePasswordConfirmed("Matkhau123", "Matkhau123"))
                .doesNotThrowAnyException();
    }

    @Test
    void validateStaffRole_accepts_the_four_staff_roles() {
        for (Role role : List.of(Role.WAITER, Role.CASHIER, Role.CHEF, Role.MANAGER)) {
            assertThatCode(() -> validator.validateStaffRole(role)).doesNotThrowAnyException();
        }
    }

    @Test
    void validateStaffRole_rejects_customer_and_null() {
        for (Role role : new Role[]{Role.CUSTOMER, null}) {
            assertThatThrownBy(() -> validator.validateStaffRole(role))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo("VALIDATION_ERROR");
        }
    }

    @Test
    void validateNotSelf_rejects_same_id() {
        UUID id = UUID.randomUUID();

        assertThatThrownBy(() -> validator.validateNotSelf(id, id))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("USER_CANNOT_MODIFY_SELF");
        assertThatCode(() -> validator.validateNotSelf(id, UUID.randomUUID())).doesNotThrowAnyException();
    }

    @Test
    void validateDeletable_passes_when_no_guard_blocks() {
        UserValidator withGuards = new UserValidator(userRepository, UserTestSupport.guards(id -> false, id -> false));

        assertThatCode(() -> withGuards.validateDeletable(UUID.randomUUID())).doesNotThrowAnyException();
    }

    @Test
    void validateDeletable_rejects_when_any_guard_blocks() {
        UserValidator withGuards = new UserValidator(userRepository, UserTestSupport.guards(id -> false, id -> true));

        assertThatThrownBy(() -> withGuards.validateDeletable(UUID.randomUUID()))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("USER_HAS_ACTIVITY");
    }
}
