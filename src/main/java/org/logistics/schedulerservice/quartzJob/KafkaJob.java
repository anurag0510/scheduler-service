package org.logistics.schedulerservice.quartzJob;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.logistics.schedulerservice.model.SchedulerEvent;
import org.logistics.schedulerservice.service.JobScheduler;
import org.logistics.schedulerservice.service.KafkaProducer;
import org.quartz.*;
import org.quartz.spi.OperableTrigger;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class KafkaJob extends QuartzJobBean {

    private KafkaProducer kafkaProducer;
    private JobScheduler jobScheduler;
    private final Integer MAX_RETRIES = 3;

    public KafkaJob(JobScheduler jobScheduler, KafkaProducer kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
        this.jobScheduler = jobScheduler;
    }

    @SneakyThrows
    @Override
    protected void executeInternal(JobExecutionContext context) {
        log.info("Inside : {}", new Object() {
        }.getClass().getEnclosingMethod().getName());
        Map<String, Object> jobDetails = context.getMergedJobDataMap().getWrappedMap();
        executeKafkaJob(jobDetails, context);
    }

    private void executeKafkaJob(Map<String, Object> jobDetails, JobExecutionContext context) throws JobExecutionException {
        log.info("Inside : {}", new Object() {
        }.getClass().getEnclosingMethod().getName());
        SchedulerEvent schedulerEvent = new SchedulerEvent();
        schedulerEvent.setRequestId(jobDetails.get("messageKey") != null ? jobDetails.get("messageKey").toString() : UUID.randomUUID().toString());
        schedulerEvent.setMessageBody(jobDetails.get("eventData"));
        schedulerEvent.setEventType(jobDetails.get("eventType").toString());
        log.info("pushing {} with event type : {}", jobDetails.get("eventData"), jobDetails.get("eventType").toString());
        try {
            kafkaProducer.sendSchedulerEvent("scheduledData", schedulerEvent);
        } catch (Exception ex) {
            handleKafkaCallFailure(context);
        }
    }

    private void handleKafkaCallFailure(JobExecutionContext context) throws JobExecutionException {
        final JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        final int retries = jobDataMap.containsKey("RETRY_COUNT") ? jobDataMap.getInt("RETRY_COUNT") : 0;
        if(retries < MAX_RETRIES) {
            log.info("retry attempt : {}", retries + 1);
            jobDataMap.put("RETRY_COUNT", retries + 1);
            JobDetail job = context.getJobDetail()
                    .getJobBuilder()
                    .withIdentity(context.getJobDetail().getKey().getName() + " - " + retries, "kafkaFailingJobs")
                    .usingJobData(context.getJobDetail().getJobDataMap())
                    .storeDurably(false)
                    .build();
            OperableTrigger retryTrigger = (OperableTrigger) TriggerBuilder
                    .newTrigger()
                    .forJob(job)
                    .startAt(new Date(context.getFireTime().getTime() + (MAX_RETRIES * 3000)))
                    .build();
            try {
                jobScheduler.getScheduler().scheduleJob(job, retryTrigger);
            } catch (SchedulerException ex) {
                throw new JobExecutionException(ex);
            }
        }
    }
}
