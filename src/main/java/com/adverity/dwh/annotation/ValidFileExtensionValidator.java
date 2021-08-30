package com.adverity.dwh.annotation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraintvalidation.SupportedValidationTarget;
import javax.validation.constraintvalidation.ValidationTarget;
import java.net.URI;

@SupportedValidationTarget(ValidationTarget.ANNOTATED_ELEMENT)
public class ValidFileExtensionValidator implements ConstraintValidator<ValidFileExtension, URI> {

    @Override
    public boolean isValid(URI uri, ConstraintValidatorContext context) {

        final var fileUriPath = uri.getPath();
        if (fileUriPath.contains(".")) {
            final var extension = fileUriPath.substring(fileUriPath.lastIndexOf("."));
            return extension.equalsIgnoreCase(".csv");
        }
        return false;
    }
}
