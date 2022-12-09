package org.logistics.schedulerservice.validation.annotation;

import org.logistics.schedulerservice.validation.definition.JobTypeValidation;
import org.logistics.schedulerservice.validation.definition.TimeStampValidation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TimeStampValidation.class)
public @interface ValidTimestamp {

    String message() default "timestamp should be of future";

    Class<?>[] groups() default {};

    public abstract Class<? extends Payload>[] payload() default {};
}
