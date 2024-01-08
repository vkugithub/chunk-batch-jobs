//package com.spring.batch.batchapplication.com.vasu.config;//package com.spring.batch.batchapplication.com.vasu.config;
//
//import org.springframework.batch.core.Job;
//import org.springframework.batch.core.Step;
//import org.springframework.batch.core.StepContribution;
//import org.springframework.batch.core.job.builder.JobBuilder;
//import org.springframework.batch.core.launch.JobLauncher;
//import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
//import org.springframework.batch.core.repository.JobRepository;
//import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
//import org.springframework.batch.core.scope.context.ChunkContext;
//import org.springframework.batch.core.step.builder.StepBuilder;
//import org.springframework.batch.core.step.tasklet.Tasklet;
//import org.springframework.batch.repeat.RepeatStatus;
//import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Profile;
//import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
//import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
//import org.springframework.transaction.PlatformTransactionManager;
//
//import javax.sql.DataSource;
//
//@Profile("spring")
//@Configuration
//public class BatchConfig {
//
//    @Bean(name = "firstBatchJob")
//    public Job job(JobRepository jobRepository, @Qualifier("step1") Step step1) {
//        return new JobBuilder("firstBatchJob", jobRepository).start(step1).build();
//    }
//
//    @Bean
//    protected Step step1(JobRepository jobRepository, PlatformTransactionManager transactionManager ) {
//        return new StepBuilder("step1", jobRepository).tasklet(firstTasklet(), transactionManager).build();
//    }
//
//    @Bean(name = "transactionManager")
//    public PlatformTransactionManager getTransactionManager() {
//        return new ResourcelessTransactionManager();
//    }
//
//    @Bean(name = "jobRepository")
//    public JobRepository getJobRepository() throws Exception {
//        JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
//        factory.setDataSource(dataSource());
//        factory.setTransactionManager(getTransactionManager());
//        factory.afterPropertiesSet();
//        return factory.getObject();
//    }
//
//    public DataSource dataSource() {
//        EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
//        return builder.setType(EmbeddedDatabaseType.H2)
//                .addScript("classpath:org/springframework/batch/core/schema-drop-h2.sql")
//                .addScript("classpath:org/springframework/batch/core/schema-h2.sql")
//                .build();
//    }
//
//    @Bean(name = "jobLauncher")
//    public JobLauncher getJobLauncher() throws Exception {
//        TaskExecutorJobLauncher jobLauncher = new TaskExecutorJobLauncher();
//        jobLauncher.setJobRepository(getJobRepository());
//        jobLauncher.afterPropertiesSet();
//        return jobLauncher;
//    }
//
//    private Tasklet firstTasklet(){
//        return new Tasklet() {
//            @Override
//            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
//                System.out.println("This is first tasklet");
//                return RepeatStatus.FINISHED;
//            }
//        };
//    }
//}
