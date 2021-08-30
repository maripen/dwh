package com.adverity.dwh.annotation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Constraint(validatedBy = ValidFileExtensionValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidFileExtension {

    String message() default
            "File must be a csv";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
