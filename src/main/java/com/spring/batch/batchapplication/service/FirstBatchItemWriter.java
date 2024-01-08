package com.spring.batch.batchapplication.service;

import com.spring.batch.batchapplication.model.StudentCsv;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.stereotype.Component;

@Component
public class FirstBatchItemWriter implements ItemWriter<StudentCsv> {
    @Override
    public void write(Chunk<? extends StudentCsv> chunk) throws Exception {

        System.out.println("Inside Item Writer");
        chunk.getItems().stream().map(item -> {
                    if(item.getId()==3)
                        throw new NullPointerException();
                    return item;
                }
        ).forEach(System.out::println);
    }
}
