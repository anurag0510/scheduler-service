package org.logistics.schedulerservice.validation.definition;

import org.logistics.schedulerservice.constants.JobType;
import org.logistics.schedulerservice.validation.annotation.RequestType;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class RequestTypeValidation implements ConstraintValidator<RequestType, String> {
    public void initialize(RequestType constraint) {
    }

    public boolean isValid(String type, ConstraintValidatorContext context) {
        if (type == null || type.length() == 0)
            return false;
        return type.equals("GET") || type.equals("POST") || type.equals("PUT") || type.equals("DELETE");
    }
}
