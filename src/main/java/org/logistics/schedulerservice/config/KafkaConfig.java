package org.logistics.schedulerservice.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.logistics.schedulerservice.model.SchedulerEvent;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Map;

@EnableKafka
@EnableRetry
@Configuration
@Slf4j
public class KafkaConfig {//implements KafkaListenerConfigurer {

    private KafkaProperties kafkaProperties;
    private LocalValidatorFactoryBean validator;

    public KafkaConfig(KafkaProperties kafkaProperties, LocalValidatorFactoryBean validator) {
        this.kafkaProperties = kafkaProperties;
        this.validator = validator;
    }

    @Bean
    public ConsumerFactory<String, SchedulerEvent> schedulerEventConsumerFactory() {
        Map<String, Object> props = kafkaProperties.buildConsumerProperties();
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new JsonDeserializer<>(SchedulerEvent.class));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, SchedulerEvent> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, SchedulerEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConcurrency(3);
        factory.setConsumerFactory(schedulerEventConsumerFactory());
        return factory;
    }

//    @Override
//    public void configureKafkaListeners(KafkaListenerEndpointRegistrar kafkaListenerEndpointRegistrar) {
//        kafkaListenerEndpointRegistrar.setValidator(this.validator);
//    }
}
