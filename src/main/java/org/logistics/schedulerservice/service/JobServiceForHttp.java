package org.logistics.schedulerservice.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import lombok.extern.slf4j.Slf4j;
import org.logistics.schedulerservice.constants.JobType;
import org.logistics.schedulerservice.exceptions.SchedulerServiceException;
import org.logistics.schedulerservice.model.*;
import org.logistics.schedulerservice.quartzJob.HttpJob;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.io.IOException;
import java.util.*;

@Slf4j
@Service
@Qualifier("httpJobService")
public class JobServiceForHttp implements JobService {

    private JobScheduler jobScheduler;
    private ValidatorFactory validatorFactory;
    private Validator validator;

    public JobServiceForHttp(JobScheduler jobScheduler) {
        this.validatorFactory = Validation.buildDefaultValidatorFactory();
        this.validator = validatorFactory.getValidator();
        this.jobScheduler = jobScheduler;
    }

    @Override
    public ScheduleResponse createSchedule(CreateScheduleRequest createScheduleRequest) {
        log.info("Inside : {}", new Object() {
        }.getClass().getEnclosingMethod().getName());
        log.info("create job and trigger request received");
        ScheduleResponse scheduleResponse = new ScheduleResponse();
        if(createScheduleRequest.getJobType().equals(JobType.HTTP.toString())) {
            try {
                HttpRequestModel httpRequestModel = validateAndReturnConvertedJobDataFromObject(createScheduleRequest.getJobData());
                JobDetail jobDetail = generateJobDetails(httpRequestModel);
                Trigger trigger = createTrigger(createScheduleRequest, jobDetail);
                jobScheduler.getScheduler().scheduleJob(jobDetail, trigger);
                scheduleResponse.setSuccess(true);
                scheduleResponse.setJobId(jobDetail.getKey().getName());
                scheduleResponse.setJobGroup(jobDetail.getKey().getGroup());
                scheduleResponse.setMessage("http job scheduled successfully");
            } catch (Exception ex) {
                ex.printStackTrace();
                scheduleResponse.setSuccess(false);
                scheduleResponse.setMessage(ex.getMessage());
            }
        } //else if (createScheduleRequest.getJobType().equals(JobType.KAFKA_PRODUCER.toString()))
//        return scheduleResponse;
        return scheduleResponse;
    }

    @Override
    public ScheduleResponse updateSchedule(UpdateScheduleRequest updateScheduleRequest) {
        log.info("Inside : {}", new Object() {
        }.getClass().getEnclosingMethod().getName());
        ScheduleResponse updateScheduleResponse = new ScheduleResponse();
        try {
            JobDetail jobDetail = getJobDetail(updateScheduleRequest.getJobId(), updateScheduleRequest.getJobGroup());
            Trigger oldTrigger = getTriggerDetail(updateScheduleRequest.getJobId(), updateScheduleRequest.getJobGroup());
            Trigger newTrigger = createTrigger(updateScheduleRequest, jobDetail);
            jobScheduler.getScheduler().rescheduleJob(oldTrigger.getKey(), newTrigger);
            updateScheduleResponse.setSuccess(true);
            updateScheduleResponse.setJobId(jobDetail.getKey().getName());
            updateScheduleResponse.setJobGroup(jobDetail.getKey().getGroup());
            updateScheduleResponse.setMessage("http job has been re-scheduled successfully");
        } catch (Exception ex) {
            updateScheduleResponse.setSuccess(false);
            updateScheduleResponse.setMessage(ex.getMessage());
        }
        return updateScheduleResponse;
    }

    @Override
    public ScheduleResponse deleteSchedule(String jobId) {
        log.info("Inside : {}", new Object() {
        }.getClass().getEnclosingMethod().getName());
        ScheduleResponse deleteScheduleResponse = new ScheduleResponse();
        try {
            Trigger jobTrigger = getTriggerDetail(jobId, "http-jobs");
            if( jobScheduler.getScheduler().unscheduleJob(jobTrigger.getKey()) &&
                    jobScheduler.getScheduler().deleteJob(new JobKey(jobId, "http-jobs"))) {
                deleteScheduleResponse.setSuccess(true);
                deleteScheduleResponse.setMessage("http job has been deleted successfully");
            } else {
                deleteScheduleResponse.setSuccess(false);
                deleteScheduleResponse.setMessage("http job removal activity failed");
            }
        }  catch (Exception ex) {
            deleteScheduleResponse.setSuccess(false);
            deleteScheduleResponse.setMessage(ex.getMessage());
        }
        return deleteScheduleResponse;
    }

    private HttpRequestModel validateAndReturnConvertedJobDataFromObject(String jobData) throws IOException {
        HttpRequestModel httpRequestModel = null;
        ObjectMapper mapper = new ObjectMapper();
//                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        ObjectReader objectReader = mapper.readerFor(HttpRequestModel.class);
        objectReader.without(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        httpRequestModel = objectReader.readValue(jobData, HttpRequestModel.class);
        Set<ConstraintViolation<HttpRequestModel>> violations = validator.validate(httpRequestModel);
        if (violations.size() != 0) {
            for (ConstraintViolation<HttpRequestModel> violation : violations)
                log.error(violation.getMessage());
            log.warn("skipping the current message due to contract violation");
            ArrayList<String> errors = new ArrayList<>();
            violations.forEach(httpRequestModelConstraintViolation -> {errors.add(httpRequestModelConstraintViolation.getMessage());});
            throw new SchedulerServiceException(String.join(", ", errors));
        }
        return httpRequestModel;
    }

    private Trigger createTrigger(RequestTriggerDetails requestTriggerDetails, JobDetail jobDetail) {
        log.info("Inside : {}", new Object() {
        }.getClass().getEnclosingMethod().getName());
        log.info("creating job trigger");
        if (requestTriggerDetails.getCronExpression() == null)
            return createSimpleTrigger(requestTriggerDetails, jobDetail);
        else if (requestTriggerDetails.getCronExpression() != null)
            return TriggerBuilder.newTrigger()
                    .withIdentity(UUID.randomUUID().toString(), "httpTrigger")
                    .withSchedule(CronScheduleBuilder.cronSchedule(requestTriggerDetails.getCronExpression()))
                    .forJob(jobDetail)
                    .build();
        else
            throw new SchedulerServiceException("error while creating trigger, missing information in request body");
    }

    private Trigger createSimpleTrigger(RequestTriggerDetails requestTriggerDetails, JobDetail jobDetail) {
        log.info("Inside : {}", new Object() {
        }.getClass().getEnclosingMethod().getName());
        TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger();
        SimpleScheduleBuilder simpleScheduleBuilder = SimpleScheduleBuilder.simpleSchedule();
        if (requestTriggerDetails.getStartTime() != null)
            triggerBuilder.startAt(new Date(requestTriggerDetails.getStartTime()));
        if (requestTriggerDetails.getEndTime() != null)
            triggerBuilder.endAt(new Date(requestTriggerDetails.getEndTime()));
        if (requestTriggerDetails.getRepeatCount() != null)
            simpleScheduleBuilder.withRepeatCount(requestTriggerDetails.getRepeatCount());
        if (requestTriggerDetails.getRepeatFrequency() != null)
            simpleScheduleBuilder.withIntervalInSeconds(requestTriggerDetails.getRepeatFrequency());
        return triggerBuilder
                .withIdentity(UUID.randomUUID().toString(), "httpTrigger")
                .withSchedule(simpleScheduleBuilder)
                .forJob(jobDetail)
                .build();
    }

    private JobDetail generateJobDetails(HttpRequestModel httpRequestModel) {
        log.info("Inside : {}", new Object() {
        }.getClass().getEnclosingMethod().getName());
        log.info("creating job details object");
        return JobBuilder.newJob(HttpJob.class)
                .withIdentity(UUID.randomUUID().toString(), "http-jobs")
                .withDescription("http job scheduling details")
                .usingJobData(generateHttpJobDataMap(httpRequestModel))
                .storeDurably()
                .requestRecovery(true)
                .build();
    }

    private JobDataMap generateHttpJobDataMap(HttpRequestModel httpRequestModel) {
        log.info("Inside : {}", new Object() {
        }.getClass().getEnclosingMethod().getName());
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("url", httpRequestModel.getUrl());
        jobDataMap.put("requestType", httpRequestModel.getRequestType());
        jobDataMap.put("headers", (httpRequestModel.getHeaders()));
        jobDataMap.put("requestBody", httpRequestModel.getRequestBody());
        jobDataMap.put("expectedStatusCode", httpRequestModel.getExpectedStatusCode());
        return jobDataMap;
    }

    private JobDetail getJobDetail(String jobId, String jobGroup) {
        return jobScheduler.getJobDetails(jobId, jobGroup);
    }

    private Trigger getTriggerDetail(String jobId, String jobGroup) {
        List<? extends Trigger> triggers = jobScheduler.getJobTriggerDetails(jobId, jobGroup);
        return triggers.get(0);
    }
}
