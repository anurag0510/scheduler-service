package org.logistics.schedulerservice.service;

import lombok.extern.slf4j.Slf4j;
import org.logistics.schedulerservice.model.SchedulerEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Service
@Slf4j
public class KafkaProducer {

    @Value("${kafka.producer.response.topic}")
    private String RESPONSE_TOPIC_NAME;

    @Value("${kafka.producer.schedule.topic}")
    private String SCHEDULE_TOPIC_NAME;

    private KafkaTemplate<String, SchedulerEvent> kafkaTemplate;

    public KafkaProducer(KafkaTemplate<String, SchedulerEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Retryable(value = Exception.class, maxAttemptsExpression = "2",
            backoff = @Backoff(delayExpression = "600000"))
    public void sendSchedulerEvent(String kafkaEventFor, SchedulerEvent schedulerProducerEvent) {
        String topicName = kafkaEventFor.equals("responseData") ? RESPONSE_TOPIC_NAME : kafkaEventFor.equals("scheduledData") ? SCHEDULE_TOPIC_NAME : null;
        if (topicName != null)
            kafkaTemplate.send(topicName, schedulerProducerEvent.getRequestId(), schedulerProducerEvent);
//            kafkaTemplate.send(topicName, schedulerProducerEvent.getRequestId(), schedulerProducerEvent);
    }
}
