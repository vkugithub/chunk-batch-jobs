package com.spring.batch.batchapplication.model.request;

import lombok.Data;

@Data
public class JobRequest {

    private String jobName;
    private String fileName;
    private String fileType;
}
