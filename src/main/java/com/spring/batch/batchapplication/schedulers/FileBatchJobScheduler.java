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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Slf4j
public class FileBatchJobScheduler {

    @Value("${input.csv.file-path}")
    private String inputCsvFilePath;

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job csvFileBatchJob;

    @Scheduled(cron = "0 0/1 * 1/1 * ?")
    public void startChunkJob(){
        log.info(" Batch job was started by scheduler");
        try {
            Map<String, JobParameter<?>> params = new HashMap();
            var inputFile = getFileName(inputCsvFilePath);
//            params.put("run.id", new JobParameter(13, Long.class));
            params.put("inputFile", new JobParameter(inputFile, String.class));
            var jobParameters = new JobParameters(params);
            jobLauncher.run(csvFileBatchJob, jobParameters);
        } catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException | JobParametersInvalidException e) {
            e.printStackTrace();
        } catch (IOException e) {
            log.error("No input CSV file");
        }
    }

    public String getFileName(String dir) throws IOException {
        try (Stream<Path> stream = Files.list(Paths.get(dir))) {
            return stream
                     .filter(file -> !Files.isDirectory(file))
                    .filter(file -> file.toString().contains(".csv"))
//                     .map(Path::getFileName)
                     .map(Path::toString)
                     .findFirst()
                     .orElseThrow();
        }
    }

}
