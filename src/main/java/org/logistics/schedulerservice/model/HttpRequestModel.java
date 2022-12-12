package org.logistics.schedulerservice.model;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.hibernate.validator.constraints.URL;
import org.logistics.schedulerservice.validation.annotation.RequestType;

import javax.validation.constraints.NotNull;
import java.util.HashMap;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class HttpRequestModel {

    @JsonProperty("request_type")
    @NotNull(message = "request_type for the job creation is required.")
    @RequestType
    private String requestType;
    @NotNull(message = "url for the job creation is required.")
    @URL(message = "url must be a valid http or https path")
    private String url;
    @JsonProperty("request_body")
    private Object requestBody;
    private HashMap<String, Object> headers;
    @JsonProperty("expected_status_code")
    private Integer expectedStatusCode;

//    @JsonAnySetter
//    public void setHeaders(String key, String value) {
//        this.headers.put(key, value);
//    }
}
