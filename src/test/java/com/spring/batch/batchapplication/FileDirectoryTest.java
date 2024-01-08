package com.spring.batch.batchapplication;

import com.spring.batch.batchapplication.schedulers.FileBatchJobScheduler;

import java.io.File;
import java.io.IOException;

public class FileDirectoryTest {
    public static void main(String[] args) {

        var scheduler = new FileBatchJobScheduler();
        try {
            var fileName = scheduler.getFileName("D:\\workspaceJava\\batch-applications\\batch-applications\\chunk-batch-jobs/OutputFiles/skiprecords");
            System.out.println("file path "+fileName);
            File fileWin = new File(fileName);
            System.out.println("fileName "+fileWin.getName());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
