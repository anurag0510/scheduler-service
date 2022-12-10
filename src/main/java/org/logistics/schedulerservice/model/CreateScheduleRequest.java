package org.logistics.schedulerservice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.*;
import org.logistics.schedulerservice.validation.annotation.SchedulerType;
import javax.validation.constraints.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(using = CreateScheduleRequestDeserializer.class)
public class CreateScheduleRequest extends RequestTriggerDetails {


    @JsonProperty("job_type")
    @NotNull(message = "job_type for the job creation is required.")
    @SchedulerType
    private String jobType;

    private String description;

    @JsonProperty("job_data")
    private String jobData;

}
