package com.spring.batch.batchapplication.config;

import com.spring.batch.batchapplication.listener.SkipListener;
import com.spring.batch.batchapplication.listener.SkipListenerImpl;
import com.spring.batch.batchapplication.model.StudentCsv;
import com.spring.batch.batchapplication.model.StudentJdbc;
import com.spring.batch.batchapplication.model.StudentJson;
import com.spring.batch.batchapplication.service.FirstBatchItemWriter;
import com.spring.batch.batchapplication.service.FirstItemProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.skip.AlwaysSkipItemSkipPolicy;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.json.JacksonJsonObjectMarshaller;
import org.springframework.batch.item.json.JsonFileItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.io.File;
import java.util.List;

//@Configuration
@Slf4j
public class ChunkJobConfig {

    @Value("${input.csv.file-path}")
    private String inputCsvFilePath;

    @Autowired
    private FirstItemProcessor firstItemProcessor;

    @Autowired
    private SkipListener skipListener;

    @Autowired
    private SkipListenerImpl skipListenerImpl;

    @Autowired
    private FirstBatchItemWriter firstBatchItemWriter;

    @Autowired
    @Qualifier("universitydatasource")
    private DataSource universitydatasource;

    @Bean("csvFileBatchJob")
    public Job job(JobRepository jobRepository, @Qualifier("processCsvStep") Step step1) {
        return new JobBuilder("csvFileBatchJob", jobRepository).incrementer(new RunIdIncrementer())
                .start(step1).build();
    }

    @Bean("processCsvStep")
    public Step processCSVFileStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("processCsvStep", jobRepository)
                .<StudentCsv, StudentCsv>chunk(3, transactionManager)
                .<StudentCsv, StudentCsv>reader(flatFileItemReader())
//                .<StudentCsv, StudentCsv>processor(firstItemProcessor)
//                .writer(jdbcBatchItemWriter())
                .writer(firstBatchItemWriter)
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(10)
//                .skipPolicy(new AlwaysSkipItemSkipPolicy())
//                .retry(NullPointerException.class)
//                .retryLimit(2)
//                .listener(skipListener)
                .listener(skipListenerImpl)
                .build();
    }

    @Bean
    public FlatFileItemReader<StudentCsv> flatFileItemReader() {
        FlatFileItemReader<StudentCsv> flatFileItemReader = new FlatFileItemReader<StudentCsv>();
        var file = new File("D:\\workspaceJava\\batch-applications\\batch-applications\\chunk-batch-jobs\\InputFiles\\students.csv");
        var file1 = new File(inputCsvFilePath);

        log.info(" inputCsvFilePath : {}",file.getPath());
        log.info(" inputCsvFilePath1 : {}",file1.getPath());
        log.info(" inputCsv absolute FilePath : {}",file1.getAbsoluteFile());
        var fileSystemResource = new FileSystemResource(file1);
        flatFileItemReader.setResource(fileSystemResource);

        flatFileItemReader.setLineMapper(new DefaultLineMapper<StudentCsv>() {
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
        });

        flatFileItemReader.setLinesToSkip(1);

        return flatFileItemReader;
    }

    @Bean
    public ItemWriter<StudentCsv> jdbcBatchItemWriter() {
        JdbcBatchItemWriter<StudentCsv> jdbcBatchItemWriter =
                new JdbcBatchItemWriter<StudentCsv>();

        jdbcBatchItemWriter.setDataSource(universitydatasource);
        jdbcBatchItemWriter.setSql(
                "insert into student(id, first_name, last_name, email) "
                        + "values (:id, :firstName, :lastName, :email)");

        jdbcBatchItemWriter.setItemSqlParameterSourceProvider(
                new BeanPropertyItemSqlParameterSourceProvider<StudentCsv>());

        return jdbcBatchItemWriter;
    }
//    @StepScope
//    @Bean
    public JsonFileItemWriter<StudentJson> jsonFileItemWriter() {

        FileSystemResource fileSystemResource = new FileSystemResource("D:\\workspaceJava\\batch-applications\\batch-applications\\chunk-batch-jobs\\OutputFiles\\students.json");

        JsonFileItemWriter<StudentJson> jsonFileItemWriter =
                new JsonFileItemWriter<StudentJson>(fileSystemResource,
                        new JacksonJsonObjectMarshaller<StudentJson>());

        return jsonFileItemWriter;
    }

    @StepScope
    @Bean
    public JsonFileItemWriter<StudentJdbc> jsonFileItemWriter(
            @Value("#{jobParameters['outputFile']}") FileSystemResource fileSystemResource) {
        JsonFileItemWriter<StudentJdbc> jsonFileItemWriter =
                new JsonFileItemWriter<>(fileSystemResource,
                        new JacksonJsonObjectMarshaller<StudentJdbc>());

        return jsonFileItemWriter;
    }

}
