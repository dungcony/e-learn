package com.elearning.common.config;

import com.elearning.common.exception.BusinessException;
import com.elearning.common.response.PageRequestParams;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PageRequestParamsArgumentResolverTest {

    private final PageRequestParamsArgumentResolver resolver = new PageRequestParamsArgumentResolver();

    @Test
    void resolveArgument_readsSnakeCaseQueryParams() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("page", "2");
        request.setParameter("page_size", "5");
        request.setParameter("sort_by", "email");
        request.setParameter("sort_order", "asc");

        PageRequestParams params = (PageRequestParams) resolver.resolveArgument(null, null, new ServletWebRequest(request), null);

        assertThat(params).isEqualTo(new PageRequestParams(2, 5, "email", "asc"));
    }

    @Test
    void resolveArgument_usesDefaultsWhenAbsent() {
        PageRequestParams params = (PageRequestParams) resolver.resolveArgument(
                null, null, new ServletWebRequest(new MockHttpServletRequest()), null);

        assertThat(params.page()).isEqualTo(PageRequestParams.DEFAULT_PAGE);
        assertThat(params.pageSize()).isEqualTo(PageRequestParams.DEFAULT_PAGE_SIZE);
    }

    @Test
    void resolveArgument_nonNumericPageThrowsValidationError() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("page_size", "abc");

        assertThatThrownBy(() -> resolver.resolveArgument(null, null, new ServletWebRequest(request), null))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getCode()).isEqualTo("VALIDATION_ERROR"));
    }
}
