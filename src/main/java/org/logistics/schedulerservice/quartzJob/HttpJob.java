package org.logistics.schedulerservice.quartzJob;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.logistics.schedulerservice.service.JobScheduler;
import org.quartz.*;
import org.quartz.spi.OperableTrigger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class HttpJob extends QuartzJobBean {

    private RestTemplate restTemplate;
    private JobScheduler jobScheduler;
    private final Integer MAX_RETRIES = 3;

    public HttpJob(JobScheduler jobScheduler) {
        this.restTemplate = new RestTemplate();
        this.jobScheduler = jobScheduler;
    }

    @SneakyThrows
    @Override
    protected void executeInternal(JobExecutionContext context) {
        log.info("Inside : {}", new Object() {
        }.getClass().getEnclosingMethod().getName());
        Map<String, Object> jobDetails = context.getMergedJobDataMap().getWrappedMap();
        executeHttpJob(jobDetails, context);
    }

    private void executeHttpJob(Map<String, Object> jobDetails, JobExecutionContext context) throws JsonProcessingException, JobExecutionException {
        log.info("Inside : {}", new Object() {
        }.getClass().getEnclosingMethod().getName());
        HttpEntity<?> entity = new HttpEntity<>(jobDetails.get("requestBody"), setHeadersForRequest(jobDetails));
        ResponseEntity<Object> response = null;
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(jobDetails.get("url").toString());
        log.info("making {} call to {}", jobDetails.get("requestType"), jobDetails.get("url"));
        String requestType = jobDetails.get("requestType").toString();
        try {
            response = restTemplate.exchange(
                    builder.toUriString(),
                    requestType.equals("GET") ? HttpMethod.GET : requestType.equals("POST") ? HttpMethod.POST : requestType.equals("PUT") ? HttpMethod.PUT : HttpMethod.DELETE,
                    entity,
                    Object.class
            );
            log.info("response : {}", response.getBody());
        } catch (HttpClientErrorException ex) {
            if (jobDetails.get("expectedStatusCode") != null) {
                if (ex.getStatusCode().value() != (Integer) jobDetails.get("expectedStatusCode")) {
                    log.warn("response data : {}", ex.getResponseBodyAsString());
                    log.warn("response status error of : {} occurred gonna retry if retry limit not breached", ex.getStatusCode().value());
                    handleHttpCallFailure(context);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private HttpHeaders setHeadersForRequest(Map<String, Object> jobDetails) throws JsonProcessingException {
        log.info("Inside : {}", new Object() {
        }.getClass().getEnclosingMethod().getName());
        HttpHeaders httpHeaders = new HttpHeaders();
        HashMap<String, String> map = new HashMap<>();
        for (Object data : ((HashMap<String, Object>) jobDetails.get("headers")).keySet()) {
            String value;
            if (((HashMap<String, Object>) jobDetails.get("headers")).get(data.toString()) instanceof String) {
                value = ((HashMap<String, Object>) jobDetails.get("headers")).get(data.toString()).toString();
            } else {
                value = new ObjectMapper().writeValueAsString(((HashMap<String, Object>) jobDetails.get("headers")).get(data.toString()));
            }
            map.put(data.toString(), value);
        }
        for (String key : map.keySet()) {
            String value = map.get(key);
            httpHeaders.add(key, value);
        }
        return httpHeaders;
    }

    private void handleHttpCallFailure(JobExecutionContext context) throws JobExecutionException {
        final JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        final int retries = jobDataMap.containsKey("RETRY_COUNT") ? jobDataMap.getInt("RETRY_COUNT") : 0;
        if(retries < MAX_RETRIES) {
            log.info("retry attempt : {}", retries + 1);
            jobDataMap.put("RETRY_COUNT", retries + 1);
            JobDetail job = context.getJobDetail()
                    .getJobBuilder()
                    .withIdentity(context.getJobDetail().getKey().getName() + " - " + retries, "httpFailingJobs")
                    .usingJobData(context.getJobDetail().getJobDataMap())
                    .storeDurably(false)
                    .build();
            OperableTrigger retryTrigger = (OperableTrigger) TriggerBuilder
                    .newTrigger()
                    .forJob(job)
                    .startAt(new Date(context.getFireTime().getTime() + (MAX_RETRIES *1000)))
                    .build();
            try {
                jobScheduler.getScheduler().scheduleJob(job, retryTrigger);
            } catch (SchedulerException ex) {
                throw new JobExecutionException(ex);
            }
        }
    }
}
