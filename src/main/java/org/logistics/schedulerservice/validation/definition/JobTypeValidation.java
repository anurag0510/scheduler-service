package org.logistics.schedulerservice.validation.definition;

import org.logistics.schedulerservice.constants.JobType;
import org.logistics.schedulerservice.validation.annotation.SchedulerType;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class JobTypeValidation implements ConstraintValidator<SchedulerType, String> {
    @Override
    public void initialize(SchedulerType constraintAnnotation) {
    }

    @Override
    public boolean isValid(String type, ConstraintValidatorContext constraintValidatorContext) {
        if (type == null || type.length() == 0)
            return false;
        return type.equals(JobType.HTTP.name()) || type.equals(JobType.KAFKA_PRODUCER.name());
    }
}
