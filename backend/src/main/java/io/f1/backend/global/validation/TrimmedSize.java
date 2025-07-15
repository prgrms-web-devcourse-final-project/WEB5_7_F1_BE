package io.f1.backend.global.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = TrimmedSizeValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface TrimmedSize {

    String message() default "공백 제외 길이가 {min}자 이상 {min}자 이하여야 합니다.";

    int min() default 0;
    int max() default 50;

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

}
