package org.logistics.schedulerservice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.*;
import org.logistics.schedulerservice.validation.annotation.ValidTimestamp;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(using = UpdateScheduledRequestDeserializer.class)
public class UpdateScheduleRequest extends RequestTriggerDetails {

    @JsonProperty("job_id")
    @NotNull
    @NotEmpty
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String jobId;

    @JsonProperty("job_group")
    @NotNull
    @NotEmpty
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String jobGroup;

}
