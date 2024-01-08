package com.spring.batch.batchapplication.service.tasklet;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ReprocessSkipRecordsTasklet implements Tasklet {

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        var stepContext = chunkContext.getStepContext();
        var jobExecutionContext = stepContext.getJobExecutionContext();
        var skipFilePath = jobExecutionContext.get("skipFilePath").toString();
        log.info("skipFilePath : {}",skipFilePath);
        var fileName = skipFilePath.substring(skipFilePath.lastIndexOf("/")+1, skipFilePath.length());
        stepContext.getStepExecutionContext().put("skipRecordFileName",fileName);

        return RepeatStatus.FINISHED;
    }
}
