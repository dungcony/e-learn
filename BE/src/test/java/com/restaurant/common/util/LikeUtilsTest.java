package com.restaurant.common.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LikeUtilsTest {

    @Test
    void contains_lowercasesTrimsAndWrapsWithWildcards() {
        assertThat(LikeUtils.contains("  Nguyễn ")).isEqualTo("%nguyễn%");
    }

    @Test
    void contains_escapesLikeWildcardsSoUserInputIsLiteral() {
        assertThat(LikeUtils.contains("50%_off\\")).isEqualTo("%50\\%\\_off\\\\%");
    }
}
