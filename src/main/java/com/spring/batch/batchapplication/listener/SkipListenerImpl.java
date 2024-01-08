package com.spring.batch.batchapplication.listener;

import com.spring.batch.batchapplication.model.StudentCsv;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.util.Date;

@Component
public class SkipListenerImpl implements SkipListener<StudentCsv, StudentCsv> {

    @Override
    public void onSkipInRead(Throwable th) {
        if(th instanceof FlatFileParseException) {
            createFile("D:\\workspaceJava\\batch-applications\\batch-applications\\chunk-batch-jobs\\Chunk Job\\First Chunk Step\\reader\\SkipInRead.txt",
                    ((FlatFileParseException) th).getInput());
        }
    }

    @Override
    public void onSkipInWrite(StudentCsv item, Throwable t) {
        createFile("D:\\workspaceJava\\batch-applications\\batch-applications\\chunk-batch-jobs\\Chunk Job\\First Chunk Step\\writer\\SkipInWrite.txt",
                item.toString());
    }

    @Override
    public void onSkipInProcess(StudentCsv item, Throwable t) {
        createFile("D:\\workspaceJava\\batch-applications\\batch-applications\\chunk-batch-jobs\\Chunk Job\\First Chunk Step\\processer\\SkipInProcess.txt",
                item.toString());
    }

    public void createFile(String filePath, String data) {
        try(FileWriter fileWriter = new FileWriter(new File(filePath), true)) {
            fileWriter.write(data + "," + new Date() + "\n");
        }catch(Exception e) {

        }
    }
}
