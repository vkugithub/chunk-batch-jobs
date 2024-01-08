package com.spring.batch.batchapplication.controller;

import com.spring.batch.batchapplication.model.request.JobRequest;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequestMapping("/api/batch")
@RestController
public class BatchController {

    @Value("${output.skip.record.file-path}")
    private String skipCsvFilePath;

    @Value("${input.csv.file-path}")
    private String inputCsvFilePath;

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job csvFileBatchJob;

    @Autowired
    private Job secondBatchJob;

    @RequestMapping(value = "/health")
    public String startSecondJob(@RequestBody String jobName) {
        return "jobStarted";
    }

    @PostMapping(value = "/job/start")
    public String startSecondJob(@RequestBody JobRequest jobRequest) {

        var batchJob= getStringBatchJob(jobRequest.getJobName());
        launchJob(jobRequest, batchJob);
        return "jobStarted";
    }

    @Async
    private void launchJob(JobRequest jobRequest, Job batchJob) {
        var jobName = jobRequest.getJobName();

        JobParameters jobParameters = getJobParameters(jobRequest);
        try {
            log.info("Spring batch job : {} got started by rest api", jobName);
            jobLauncher.run(batchJob, jobParameters);
        } catch (JobExecutionAlreadyRunningException | JobInstanceAlreadyCompleteException | JobParametersInvalidException | JobRestartException ex) {
            log.error("Exception to start the batch job : {}", jobName, ex);
        }
    }

    private JobParameters getJobParameters(JobRequest jobRequest) {

        var jobFilePath = "skip".equalsIgnoreCase(jobRequest.getFileType()) ? skipCsvFilePath : inputCsvFilePath;
        Map<String, JobParameter<?>> params = new HashMap();
//        params.put("currentTime", new JobParameter(System.currentTimeMillis(), Long.class));
        params.put("inputFile", new JobParameter(jobFilePath+ jobRequest.getFileName(), String.class));

        var jobParameters = new JobParameters(params);
        return jobParameters;
    }

    private Job getStringBatchJob(String jobName){
        if("fileProcess".equalsIgnoreCase(jobName))
            return csvFileBatchJob;
        else if("secondJob".equalsIgnoreCase(jobName))
            return secondBatchJob;
        else
            throw new RuntimeException("Invalid job name in request");
    }
}
