package com.spring.batch.batchapplication.schedulers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

//@Component
@Slf4j
public class ChunkOrientedBatchJobScheduler {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job firstBatchJob;

    @Scheduled(cron = "0 0/1 * 1/1 * ?")
    public void startChunkJob(){
        log.info(" Batch job was started by com.vasu.scheduler");
        Map<String, JobParameter<?>> params = new HashMap();
        params.put("currentTime", new JobParameter(System.currentTimeMillis(), Long.class));
        JobParameters jobParameters = new JobParameters(params);

        try {
            jobLauncher.run(firstBatchJob, jobParameters);
        } catch (JobExecutionAlreadyRunningException e) {
            e.printStackTrace();
        } catch (JobRestartException e) {
            e.printStackTrace();
        } catch (JobInstanceAlreadyCompleteException e) {
            e.printStackTrace();
        } catch (JobParametersInvalidException e) {
            e.printStackTrace();
        }
    }
}
