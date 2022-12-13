package org.logistics.schedulerservice.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.logistics.schedulerservice.exceptions.SchedulerServiceException;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;

@JsonComponent
public class CreateScheduleRequestDeserializer extends StdDeserializer<CreateScheduleRequest> {

    protected CreateScheduleRequestDeserializer() {
        super(CreateScheduleRequest.class);
    }

    @Override
    public CreateScheduleRequest deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        ObjectMapper mapper = (ObjectMapper) jsonParser.getCodec();
        JsonNode tree = mapper.readTree(jsonParser);

        CreateScheduleRequest createScheduleRequest = new CreateScheduleRequest();
        JsonNode cronExpressionNode = tree.get("cron_expression");
        JsonNode startTimeNode = tree.get("start_time");
        JsonNode endTimeNode = tree.get("end_time");
        JsonNode repeatCountNode = tree.get("repeat_count");
        JsonNode repeatFrequencyNode = tree.get("repeat_frequency");
        JsonNode jobTypeNode = tree.get("job_type");
        JsonNode descriptionNode = tree.get("description");
        JsonNode jobDataNode = tree.get("job_data");

        if (cronExpressionNode == null || cronExpressionNode.asText().isEmpty()) {
            if((repeatCountNode == null || repeatCountNode.asInt() == 0) && (repeatFrequencyNode == null || repeatFrequencyNode.asInt() == 0))
            throw new SchedulerServiceException("if cron_expression is not provided please provide repeat_count or repeat_frequency to create a scheduler");
        }
        createScheduleRequest.setCronExpression(cronExpressionNode == null ? null : cronExpressionNode.asText());
        createScheduleRequest.setStartTime(startTimeNode == null ? null : startTimeNode.asLong());
        createScheduleRequest.setEndTime(endTimeNode == null ? null : endTimeNode.asLong());
        createScheduleRequest.setRepeatFrequency(repeatCountNode == null ? null : repeatFrequencyNode.asInt());
        createScheduleRequest.setRepeatCount(repeatCountNode == null ? null : repeatCountNode.asInt());
        createScheduleRequest.setJobType(jobTypeNode == null ? null : jobTypeNode.asText());
        createScheduleRequest.setDescription(descriptionNode == null ? null : descriptionNode.asText());
        createScheduleRequest.setJobData(jobDataNode == null ? null : "" + jobDataNode);
        return createScheduleRequest;
    }
}
