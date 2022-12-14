package org.logistics.schedulerservice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.logistics.schedulerservice.validation.annotation.ValidTimestamp;

import javax.validation.constraints.Min;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestTriggerDetails {

    @JsonProperty("cron_expression")
    private String cronExpression;

    @JsonProperty("start_time")
    @ValidTimestamp(message = "start_time should be of future timestamp")
    private Long startTime;

    @JsonProperty("end_time")
    @ValidTimestamp(message = "end_time should be of future timestamp")
    private Long endTime;

    @JsonProperty("repeat_count")
    @Min(value = 1, message = "minimum value of 1 is required")
    private Integer repeatCount;

    @JsonProperty("repeat_frequency")
    @Min(value = 1, message = "minimum value of 1 is required")
    private Integer repeatFrequency;

}
