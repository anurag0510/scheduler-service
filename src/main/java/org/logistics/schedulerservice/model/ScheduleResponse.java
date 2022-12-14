package org.logistics.schedulerservice.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class ScheduleResponse {

    private Boolean success;
    private String message;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("job_id")
    private String jobId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("job_group")
    private String jobGroup;

    public ScheduleResponse(Boolean success, String message, String jobId, String jobGroup) {
        this.success = success;
        this.message = message;
        this.jobId = jobId;
        this.jobGroup = jobGroup;
    }

    public ScheduleResponse(Boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
