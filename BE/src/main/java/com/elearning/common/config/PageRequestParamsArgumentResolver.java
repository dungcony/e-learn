package com.elearning.common.config;

import com.elearning.common.exception.BusinessException;
import com.elearning.common.exception.ErrorCode;
import com.elearning.common.response.PageRequestParams;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Cho controller khai báo tham số {@link PageRequestParams} và nhận {@code page}, {@code page_size},
 * {@code sort_by}, {@code sort_order} từ query string (snake_case, rule 2.8).
 */
public class PageRequestParamsArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return PageRequestParams.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        return PageRequestParams.of(
                toInteger(webRequest.getParameter("page"), "page"),
                toInteger(webRequest.getParameter("page_size"), "page_size"),
                webRequest.getParameter("sort_by"),
                webRequest.getParameter("sort_order"));
    }

    private Integer toInteger(String value, String name) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Tham số " + name + " phải là số nguyên.");
        }
    }
}
