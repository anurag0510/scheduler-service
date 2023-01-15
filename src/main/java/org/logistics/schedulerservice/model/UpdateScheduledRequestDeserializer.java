package org.logistics.schedulerservice.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.logistics.schedulerservice.exceptions.SchedulerServiceException;

import java.io.IOException;

public class UpdateScheduledRequestDeserializer extends StdDeserializer<UpdateScheduleRequest> {

    protected UpdateScheduledRequestDeserializer() {
        super(UpdateScheduleRequest.class);
    }

    @Override
    public UpdateScheduleRequest deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        ObjectMapper mapper = (ObjectMapper) jsonParser.getCodec();
        JsonNode tree = mapper.readTree(jsonParser);

        UpdateScheduleRequest updateScheduleRequest = new UpdateScheduleRequest();
        JsonNode cronExpressionNode = tree.get("cron_expression");
        JsonNode startTimeNode = tree.get("start_time");
        JsonNode endTimeNode = tree.get("end_time");
        JsonNode repeatCountNode = tree.get("repeat_count");
        JsonNode repeatFrequencyNode = tree.get("repeat_frequency");
        JsonNode jobIdNode = tree.get("job_id");
        JsonNode jobGroupNode = tree.get("job_group");


        if (cronExpressionNode == null || cronExpressionNode.asText().isEmpty()) {
            if ((repeatCountNode == null || repeatCountNode.asInt() == 0) && (repeatFrequencyNode == null || repeatFrequencyNode.asInt() == 0))
                throw new SchedulerServiceException("if cron_expression is not provided please provide repeat_count or repeat_frequency to update a scheduler");
        }
        updateScheduleRequest.setCronExpression(cronExpressionNode == null ? null : cronExpressionNode.asText());
        updateScheduleRequest.setStartTime(startTimeNode == null ? null : startTimeNode.asLong());
        updateScheduleRequest.setEndTime(endTimeNode == null ? null : endTimeNode.asLong());
        updateScheduleRequest.setRepeatFrequency(repeatCountNode == null ? null : repeatFrequencyNode.asInt());
        updateScheduleRequest.setRepeatCount(repeatCountNode == null ? null : repeatCountNode.asInt());
        updateScheduleRequest.setJobId(jobIdNode == null ? null : jobIdNode.asText());
        updateScheduleRequest.setJobGroup(jobGroupNode == null ? null : jobGroupNode.asText());
        return updateScheduleRequest;
    }
}
