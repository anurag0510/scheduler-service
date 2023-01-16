package org.logistics.schedulerservice.service;

import org.logistics.schedulerservice.model.CreateScheduleRequest;
import org.logistics.schedulerservice.model.ScheduleResponse;
import org.logistics.schedulerservice.model.UpdateScheduleRequest;

public interface JobService {

    ScheduleResponse createSchedule(CreateScheduleRequest createScheduleRequest);

    ScheduleResponse updateSchedule(UpdateScheduleRequest updateScheduleRequest);

    ScheduleResponse deleteSchedule(String jobId);
}
