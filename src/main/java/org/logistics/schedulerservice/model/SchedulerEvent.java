package org.logistics.schedulerservice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;


@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties("message_key")
public class SchedulerEvent {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @NotNull(message = "from_service must not be null")
    @JsonProperty("from_service")
    private String fromService;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("for_service")
    private String forService;

    @JsonProperty("request_id")
    @Pattern(regexp = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")
    @NotNull(message = "request_id must not be null")
    private String requestId;

    @JsonProperty("event_type")
    @NotNull
    private String eventType;

    private Long timestamp = System.currentTimeMillis();

    @JsonProperty("message_body")
    @NotNull
    private Object messageBody;


}
