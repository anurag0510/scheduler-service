package org.logistics.schedulerservice.validation.definition;

import org.logistics.schedulerservice.validation.annotation.RequestType;
import org.logistics.schedulerservice.validation.annotation.ValidTimestamp;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.annotation.Annotation;

public class TimeStampValidation implements ConstraintValidator<ValidTimestamp, Long> {
    @Override
    public void initialize(ValidTimestamp constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Long timestamp, ConstraintValidatorContext constraintValidatorContext) {
        if(timestamp == null)
            return true;
        return System.currentTimeMillis() < timestamp;
    }
}
