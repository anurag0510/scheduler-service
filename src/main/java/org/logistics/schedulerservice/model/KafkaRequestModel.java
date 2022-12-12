package org.logistics.schedulerservice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class KafkaRequestModel {


    @JsonProperty("event_type")
    @NotNull(message = "event_type for the job creation is required.")
    private String eventType;

    @JsonProperty("message_key")
    @Pattern(regexp = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}", message = "please provide message_key in correct uuid pattern")
    private String messageKey;

    @JsonProperty("event_data")
    private Object eventData;

}
