package org.logistics.schedulerservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.logistics.schedulerservice.model.CreateScheduleRequest;
import org.logistics.schedulerservice.model.ScheduleResponse;
import org.logistics.schedulerservice.model.UpdateScheduleRequest;
import org.logistics.schedulerservice.service.JobService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/v1/http/schedule")
public class HttpJobSchedulerController {

    private JobService jobService;

    public HttpJobSchedulerController(@Qualifier("httpJobService")JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping
    public ResponseEntity<ScheduleResponse> createSchedule(
            @Valid @RequestBody CreateScheduleRequest createScheduleRequest
    ) {
        log.info("{}", createScheduleRequest.toString());
        log.info("request received to schedule a {} job", createScheduleRequest.getJobType());
        ScheduleResponse scheduleResponse = jobService.createSchedule(createScheduleRequest);
        return new ResponseEntity<>(scheduleResponse, HttpStatus.CREATED);
    }

    @PutMapping
    public ResponseEntity<ScheduleResponse> updateSchedule(
            @Valid @RequestBody UpdateScheduleRequest updateScheduleRequest
    ) {
        log.info("{}", updateScheduleRequest.toString());
        log.info("request received to re-schedule a job with id : {} and group : {}", updateScheduleRequest.getJobId(), updateScheduleRequest.getJobGroup());
        ScheduleResponse scheduleResponse = jobService.updateSchedule(updateScheduleRequest);
        return new ResponseEntity<>(scheduleResponse, HttpStatus.OK);
    }

    @DeleteMapping(value = "/job_id/{jobId}")
    public ResponseEntity<ScheduleResponse> deleteSchedule(
            @PathVariable(value = "jobId", required = true) String jobId
    ) {
        log.info("request received to delete a job with id : {}", jobId);
        ScheduleResponse scheduleResponse = jobService.deleteSchedule(jobId);
        return new ResponseEntity<>(scheduleResponse, HttpStatus.OK);
    }
}
