package com.restaurant.common.logging;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LogMaskerTest {

    @Test
    void masks_all_password_fields_of_auth_requests() {
        String body = "{\"old_password\":\"Matkhau123\",\"new_password\":\"Matkhau456\","
                + "\"confirm_new_password\":\"Matkhau456\",\"confirm_password\":\"Matkhau789\"}";

        String masked = LogMasker.mask(body);

        assertThat(masked)
                .doesNotContain("Matkhau123", "Matkhau456", "Matkhau789")
                .contains("\"confirm_new_password\":\"[MASKED]\"");
    }

    @Test
    void masks_camel_case_confirm_new_password() {
        assertThat(LogMasker.mask("{\"confirmNewPassword\":\"Matkhau456\"}")).doesNotContain("Matkhau456");
    }

    @Test
    void masks_token_bearer_and_email() {
        String masked = LogMasker.mask("{\"token\":\"abc123\"} Authorization: Bearer eyJ.a.b user an.nguyen@gmail.com");

        assertThat(masked).doesNotContain("abc123", "eyJ.a.b", "an.nguyen@gmail.com").contains("a***@gmail.com");
    }
}
