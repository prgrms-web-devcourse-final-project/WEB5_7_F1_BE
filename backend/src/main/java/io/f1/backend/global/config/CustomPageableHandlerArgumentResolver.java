package io.f1.backend.global.config;

import io.f1.backend.global.exception.CustomException;
import io.f1.backend.global.exception.errorcode.CommonErrorCode;
import io.f1.backend.global.validation.LimitPageSize;

import org.springframework.core.MethodParameter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

public class CustomPageableHandlerArgumentResolver extends PageableHandlerMethodArgumentResolver {

    @Override
    public Pageable resolveArgument(
            MethodParameter methodParameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory) {

        Pageable pageable =
                super.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory);

        if (methodParameter.hasMethodAnnotation(LimitPageSize.class)) {
            LimitPageSize limitPageSize = methodParameter.getMethodAnnotation(LimitPageSize.class);
            validatePageable(limitPageSize, pageable);
        }

        return PageRequest.of(
                oneIndexedPageNumber(pageable.getPageNumber()), pageable.getPageSize());
    }

    private int oneIndexedPageNumber(int pageNumber) {
        return pageNumber <= 0 ? 0 : pageNumber - 1;
    }

    private void validatePageable(LimitPageSize limitPageSize, Pageable pageable) {
        if (pageable.getPageSize() > limitPageSize.max()) {
            throw new CustomException(CommonErrorCode.INVALID_PAGINATION);
        }
    }
}
