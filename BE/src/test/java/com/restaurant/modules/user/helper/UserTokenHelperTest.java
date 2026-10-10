package com.restaurant.modules.user.helper;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserTokenHelperTest {

    private final UserTokenHelper helper = new UserTokenHelper();

    @Test
    void generate_returns_url_safe_raw_token_and_matching_sha256_hash() {
        UserTokenHelper.GeneratedToken token = helper.generate();

        assertThat(token.raw()).hasSize(43).matches("[A-Za-z0-9_-]+");
        assertThat(token.hash()).hasSize(64).matches("[0-9a-f]+");
        assertThat(helper.hash(token.raw())).isEqualTo(token.hash());
    }

    @Test
    void generate_never_repeats() {
        assertThat(helper.generate().raw()).isNotEqualTo(helper.generate().raw());
    }

    @Test
    void hash_is_deterministic_sha256() {
        // SHA-256("abc") là giá trị chuẩn
        assertThat(helper.hash("abc"))
                .isEqualTo("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
    }
}
