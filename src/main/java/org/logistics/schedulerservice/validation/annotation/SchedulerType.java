package org.logistics.schedulerservice.validation.annotation;

import org.logistics.schedulerservice.validation.definition.JobTypeValidation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = JobTypeValidation.class)
public @interface SchedulerType {

    String message() default "job_type should be out of [HTTP, KAFKA_PRODUCER]";

    Class<?>[] groups() default {};

    public abstract Class<? extends Payload>[] payload() default {};
}
