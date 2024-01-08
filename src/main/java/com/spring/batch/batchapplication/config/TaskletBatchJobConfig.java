package com.spring.batch.batchapplication.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

//@Configuration
@Slf4j
public class TaskletBatchJobConfig {


    @Bean(name = "firstBatchJob")
    public Job job(JobRepository jobRepository) {
        return new JobBuilder("firstBatchJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(step1(null, null))
                .next(step2(null, null))
                .build();
    }

    @Bean
    protected Step step1(JobRepository jobRepository, PlatformTransactionManager transactionManager ) {
        return new StepBuilder("step1", jobRepository)
                .tasklet(firstTasklet(), transactionManager)
                .build();
    }

    private Tasklet firstTasklet(){
        return new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                log.info("This is first tasklet");
                return RepeatStatus.FINISHED;
            }
        };
    }

    @Bean
    protected Step step2(JobRepository jobRepository, PlatformTransactionManager transactionManager ) {
        return new StepBuilder("step1", jobRepository)
                .tasklet(secondTasklet(), transactionManager)
                .build();
    }

    private Tasklet secondTasklet(){
        return new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                log.info("This is second tasklet");
                return RepeatStatus.FINISHED;
            }
        };
    }
}
