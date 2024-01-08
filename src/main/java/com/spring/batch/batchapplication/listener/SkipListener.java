package com.spring.batch.batchapplication.listener;

import java.io.File;
import java.io.FileWriter;
import java.util.Date;

import com.spring.batch.batchapplication.model.StudentCsv;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.OnSkipInProcess;
import org.springframework.batch.core.annotation.OnSkipInRead;
import org.springframework.batch.core.annotation.OnSkipInWrite;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@StepScope
@Component
public class SkipListener {

	@Value("${output.skip.record.file-path}")
	private String skipRecordFilePath;

	@Value("#{stepExecution}")
	private StepExecution stepExecution;

	@OnSkipInRead
	public void skipInRead(Throwable th ) {
		if(th instanceof FlatFileParseException) {
			var skipedRecord = ((FlatFileParseException) th).getInput();
//			createFile("SkipInRead.txt", skipedRecord);
			writeToSkipRecordFile(skipedRecord);
		}
	}

	@OnSkipInProcess
	public void skipInProcess(StudentCsv studentCsv, Throwable th) {
//		createFile("SkipInProcess.txt", studentCsv.toString());
		writeToSkipRecordFile(studentCsv.toString());
	}
	
	@OnSkipInWrite
	public void skipInWriter(StudentCsv studentCsv, Throwable th) {
//		createFile("SkipInWrite.txt", studentCsv.toString());
		writeToSkipRecordFile(studentCsv.toString());
	}

	private void writeToSkipRecordFile(String skipedRecord) {
		var jobExecution = stepExecution.getJobExecution();
		var jobExecutionContext = jobExecution.getExecutionContext();
		var filePath = jobExecution.getJobParameters().getParameter("inputFile").getValue().toString();
		var skipFileName = (new File(filePath)).getName();
		var file = createFile("skipFile_"+skipFileName, skipedRecord);

		jobExecutionContext.put("skipFilePath", file.getPath());
	}

	public File createFile(String fileName, String data) {
		var file = new File(skipRecordFilePath + fileName);
		try(FileWriter fileWriter = new FileWriter(file, true)) {
			fileWriter.write(data+ "\n");
//			fileWriter.write(data + "," + new Date() + "\n");
		}catch(Exception e) {
			log.error("Exception in writting skip file",e);
		}
		return file;
	}
}
