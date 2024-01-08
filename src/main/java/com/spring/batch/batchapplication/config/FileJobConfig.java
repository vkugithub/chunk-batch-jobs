package com.spring.batch.batchapplication.config;

import com.spring.batch.batchapplication.listener.SkipListener;
import com.spring.batch.batchapplication.listener.SkipListenerImpl;
import com.spring.batch.batchapplication.model.StudentCsv;
import com.spring.batch.batchapplication.model.StudentJdbc;
import com.spring.batch.batchapplication.model.StudentJson;
import com.spring.batch.batchapplication.service.FileDataProcessor;
import com.spring.batch.batchapplication.service.FirstBatchItemWriter;
import com.spring.batch.batchapplication.service.FirstItemProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.skip.AlwaysSkipItemSkipPolicy;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.json.JacksonJsonObjectMarshaller;
import org.springframework.batch.item.json.JsonFileItemWriter;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.io.File;

@Configuration
@Slf4j
public class FileJobConfig {

    @Value("${input.csv.file-path}")
    private String inputCsvFilePath;

    @Autowired
    private FileDataProcessor fileDataProcessor;

    @Autowired
    private SkipListener skipListener;

    @Autowired
    @Qualifier("reprocessSkipRecordsTasklet")
    private Tasklet reprocessSkipRecordsTasklet;

    @Autowired
    @Qualifier("sendEmailForSkipRecordsTasklet")
    private Tasklet sendEmailForSkipRecordsTasklet;

//    @Autowired
//    private SkipListenerImpl skipListenerImpl;

//    @Autowired
//    private FirstBatchItemWriter firstBatchItemWriter;

    @Autowired
    @Qualifier("universitydatasource")
    private DataSource universitydatasource;

    @Bean("csvFileBatchJob")
    public Job job(JobRepository jobRepository, @Qualifier("processCsvStep") Step step) {
        return new JobBuilder("csvFileBatchJob", jobRepository)
                    .incrementer(new RunIdIncrementer())
                    .start(step)
                    .next(sendEmailForSkipRecordsStep(null, null))
                    .build();
    }

    @Bean("processCsvStep")
    public Step processCSVFileStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("processCsvStep", jobRepository)
                .<StudentCsv, StudentCsv>chunk(3, transactionManager)
                .<StudentCsv, StudentCsv>reader(flatFileItemReader(null))
                .<StudentCsv, StudentCsv>processor(fileDataProcessor)
                .writer(jdbcBatchItemWriter())
                .faultTolerant()
                .skip(Exception.class)
//                .skipLimit(10)
                .skipPolicy(new AlwaysSkipItemSkipPolicy())
//                .retry(NullPointerException.class)
//                .retry(Exception.class)
//                .retryLimit(3)
                .listener(skipListener)
//                .listener(skipListenerImpl)
                .build();
    }

    @Bean("reprocessSkipRecordsStep")
    public Step retrySkipRecordsStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("reprocessSkipRecordsStep", jobRepository)
                .tasklet(reprocessSkipRecordsTasklet, transactionManager)
                .build();
    }

    @Bean("sendEmailForSkipRecordsStep")
    public Step sendEmailForSkipRecordsStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("sendEmailForSkipRecordsStep", jobRepository)
                .tasklet(sendEmailForSkipRecordsTasklet, transactionManager)
                .build();
    }

    @StepScope
    @Bean
    public FlatFileItemReader<StudentCsv> flatFileItemReader(@Value("#{jobParameters['inputFile']}") String filePath) {

        FlatFileItemReader<StudentCsv> flatFileItemReader = new FlatFileItemReader<StudentCsv>();
        var file = new File(StringUtils.defaultIfBlank(filePath, inputCsvFilePath));
        log.info(" inputCsvFilePath1 : {}",file.getPath());
        var fileSystemResource = new FileSystemResource(file);
        flatFileItemReader.setResource(fileSystemResource);
        flatFileItemReader.setLineMapper(getLineMapper());
        flatFileItemReader.setLinesToSkip(1);

        return flatFileItemReader;

    }

    private DefaultLineMapper<StudentCsv> getLineMapper() {
        return new DefaultLineMapper<StudentCsv>() {
            {
                setLineTokenizer(new DelimitedLineTokenizer() {
                    {
                        setNames("ID", "First Name", "Last Name", "Email");
                    }
                });

                setFieldSetMapper(new BeanWrapperFieldSetMapper<StudentCsv>() {
                    {
                        setTargetType(StudentCsv.class);
                    }
                });

            }
        };
    }

    @Bean
    public ItemWriter<StudentCsv> jdbcBatchItemWriter() {
        JdbcBatchItemWriter<StudentCsv> jdbcBatchItemWriter =
                new JdbcBatchItemWriter<StudentCsv>();

        jdbcBatchItemWriter.setDataSource(universitydatasource);
        jdbcBatchItemWriter.setSql(
                "insert into employee(id, first_name, last_name, email, created_on) "
                        + "values (:id, :firstName, :lastName, :email, now())");

        jdbcBatchItemWriter.setItemSqlParameterSourceProvider(
                new BeanPropertyItemSqlParameterSourceProvider<StudentCsv>());

        return jdbcBatchItemWriter;
    }

}
