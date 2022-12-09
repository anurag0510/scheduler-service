package org.logistics.schedulerservice.validation.annotation;

import org.logistics.schedulerservice.validation.definition.JobTypeValidation;
import org.logistics.schedulerservice.validation.definition.RequestTypeValidation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = RequestTypeValidation.class)
public @interface RequestType {

    String message() default "request_type should be out of [GET, POST, PUT, DELETE]";

    Class<?>[] groups() default {};

    public abstract Class<? extends Payload>[] payload() default {};
}
