package org.logistics.schedulerservice.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.logistics.schedulerservice.exceptions.SchedulerServiceException;
import org.quartz.*;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Arrays;
import java.util.List;

@Service
@Getter
@Slf4j
public class JobScheduler {

    private Scheduler scheduler;

    public JobScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @PostConstruct
    private void initializeScheduler() throws SchedulerException {
        this.scheduler.start();
    }

    @PreDestroy
    private void shutDownScheduler() throws SchedulerException {
        this.scheduler.shutdown();
    }

    public JobDetail getJobDetails(String jobId, String jobGroup) {
        JobKey jobKey = new JobKey(jobId, jobGroup);
        try {
            return this.scheduler.getJobDetail(jobKey);
        } catch (Exception ex) {
            log.info(Arrays.toString(ex.getStackTrace()));
            throw new SchedulerServiceException(ex.getMessage());
        }
    }

    public List<? extends Trigger> getJobTriggerDetails(String jobId, String jobGroup) {
        JobKey jobKey = new JobKey(jobId, jobGroup);
        try {
            return this.scheduler.getTriggersOfJob(jobKey);
        } catch (Exception ex) {
            log.info(Arrays.toString(ex.getStackTrace()));
            throw new SchedulerServiceException(ex.getMessage());
        }
    }
}
