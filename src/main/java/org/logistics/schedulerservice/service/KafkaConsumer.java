package org.logistics.schedulerservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.logistics.schedulerservice.constants.EventType;
import org.logistics.schedulerservice.model.CreateScheduleRequest;
import org.logistics.schedulerservice.model.ScheduleResponse;
import org.logistics.schedulerservice.model.SchedulerEvent;
import org.logistics.schedulerservice.model.UpdateScheduleRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import javax.validation.*;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Service
@Slf4j
public class KafkaConsumer {

    private ValidatorFactory validatorFactory;
    private Validator validator;
    private JobService httpJobService;
    private JobService kafkaJobService;
    private KafkaProducer kafkaProducer;

    public KafkaConsumer(@Qualifier("httpJobService") JobService httpJobService, @Qualifier("kafkaJobService") JobService kafkaJobService, KafkaProducer kafkaProducer) {
        this.validatorFactory = Validation.buildDefaultValidatorFactory();
        this.validator = validatorFactory.getValidator();
        this.httpJobService = httpJobService;
        this.kafkaJobService = kafkaJobService;
        this.kafkaProducer = kafkaProducer;
    }

    @KafkaListener(topics = "scheduling-data", containerFactory = "kafkaListenerContainerFactory")
    public void consume(@Payload SchedulerEvent kafkaData) {
        log.info("{}", kafkaData);
        Set<ConstraintViolation<SchedulerEvent>> violations = validator.validate(kafkaData);
        if (violations.size() != 0) {
            for (ConstraintViolation<SchedulerEvent> violation : violations)
                log.error(violation.getMessage());
            log.warn("skipping the current message due to contract violation");
        } else {
            if (kafkaData.getEventType().equals(EventType.CREATE_SCHEDULE.toString())) {
                ScheduleResponse scheduleResponse = createSchedule(kafkaData.getMessageBody());
                populateAndProduceEvent(kafkaData, scheduleResponse, "create_schedule_response");
            } else if (kafkaData.getEventType().equals(EventType.UPDATE_SCHEDULE.toString())) {
                ScheduleResponse scheduleResponse = updateSchedule(kafkaData.getMessageBody());
                populateAndProduceEvent(kafkaData, scheduleResponse, "update_schedule_response");
            } else {
                ScheduleResponse scheduleResponse = deleteSchedule(kafkaData.getMessageBody());
                populateAndProduceEvent(kafkaData, scheduleResponse, "delete_schedule_response");
            }
        }
    }

    private void populateAndProduceEvent(SchedulerEvent schedulerEvent, ScheduleResponse scheduleResponse, String eventType) {
        SchedulerEvent schedulerProducerEvent = new SchedulerEvent();
        schedulerProducerEvent.setForService(schedulerEvent.getFromService());
        schedulerProducerEvent.setRequestId(schedulerEvent.getRequestId());
        schedulerProducerEvent.setEventType(eventType);
        schedulerProducerEvent.setMessageBody(scheduleResponse);
        log.info("{}", schedulerProducerEvent);
        kafkaProducer.sendSchedulerEvent("responseData", schedulerProducerEvent);
    }

    private ScheduleResponse deleteSchedule(Object deleteScheduleObject) {
        ScheduleResponse deleteScheduleResponse = new ScheduleResponse();
        try {
            String jobIdToDelete = new ObjectMapper().convertValue(deleteScheduleObject, Map.class).get("job_id").toString();
            if (jobIdToDelete == null || !Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}").matcher(jobIdToDelete).matches()) {
                log.error("invalid job_id for removing job from quartz received : {}", jobIdToDelete);
                throw new Exception("payload body violated the contract");
            } else
                deleteScheduleResponse = httpJobService.deleteSchedule(jobIdToDelete);
        } catch (Exception ex) {
            deleteScheduleResponse.setSuccess(false);
            deleteScheduleResponse.setMessage(ex.getMessage());
        }
        return deleteScheduleResponse;
    }

    private ScheduleResponse updateSchedule(Object updateScheduleObject) {
        ScheduleResponse updateScheduleResponse = new ScheduleResponse();
        try {
            UpdateScheduleRequest updateScheduleRequest = new ObjectMapper().convertValue(updateScheduleObject, UpdateScheduleRequest.class);
            Set<ConstraintViolation<UpdateScheduleRequest>> violations = validator.validate(updateScheduleRequest);
            if (violations.size() != 0) {
                for (ConstraintViolation<UpdateScheduleRequest> violation : violations)
                    log.error(violation.getMessage());
                throw new Exception("payload body violated the contract");
            } else
                updateScheduleResponse = httpJobService.updateSchedule(updateScheduleRequest);
        } catch (Exception ex) {
            updateScheduleResponse.setSuccess(false);
            updateScheduleResponse.setMessage(ex.getMessage());
        }
        return updateScheduleResponse;
    }

    private ScheduleResponse createSchedule(Object createScheduleObject) {
        ScheduleResponse createScheduleResponse = new ScheduleResponse();
        try {
            CreateScheduleRequest createScheduleRequest = new ObjectMapper().convertValue(createScheduleObject, CreateScheduleRequest.class);
            Set<ConstraintViolation<CreateScheduleRequest>> violations = validator.validate(createScheduleRequest);
            if (violations.size() != 0) {
                for (ConstraintViolation<CreateScheduleRequest> violation : violations)
                    log.error(violation.getMessage());
                throw new Exception("payload body violated the contract");
            } else
                createScheduleResponse = httpJobService.createSchedule(createScheduleRequest);
        } catch (Exception ex) {
            createScheduleResponse.setSuccess(false);
            createScheduleResponse.setMessage(ex.getMessage());
        }
        return createScheduleResponse;
    }
}
