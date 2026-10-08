package com.elearning.user.validator;

import com.elearning.common.exception.BusinessException;
import com.elearning.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class UserValidatorTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserValidator validator;

    @Test
    void validatePasswordConfirm_equalPasswordsPass() {
        assertThatCode(() -> validator.validatePasswordConfirm("123456", "123456")).doesNotThrowAnyException();
    }

    @Test
    void validatePasswordConfirm_differentOrMissingConfirmationFails() {
        assertThatThrownBy(() -> validator.validatePasswordConfirm("123456", "654321"))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getCode()).isEqualTo("AUTH_PASSWORD_CONFIRM_MISMATCH"));
        assertThatThrownBy(() -> validator.validatePasswordConfirm("123456", null))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> validator.validatePasswordConfirm(null, "123456"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void validateEmailNotTaken_createChecksPlainExistence() {
        when(userRepository.existsByEmail("a@b.c")).thenReturn(true);

        assertThatThrownBy(() -> validator.validateEmailNotTaken("a@b.c", null))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getCode()).isEqualTo("AUTH_EMAIL_ALREADY_EXISTS"));
    }

    @Test
    void validateEmailNotTaken_updateExcludesOwnAccount() {
        UUID self = UUID.randomUUID();
        when(userRepository.existsByEmailAndIdNot("a@b.c", self)).thenReturn(false);

        assertThatCode(() -> validator.validateEmailNotTaken("a@b.c", self)).doesNotThrowAnyException();

        verify(userRepository).existsByEmailAndIdNot("a@b.c", self);
        verifyNoMoreInteractions(userRepository);
    }
}
