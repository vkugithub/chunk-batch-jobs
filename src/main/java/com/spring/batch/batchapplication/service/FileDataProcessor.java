package com.spring.batch.batchapplication.service;

import com.spring.batch.batchapplication.model.StudentCsv;
import com.spring.batch.batchapplication.model.StudentJson;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Date;

@Component
public class FileDataProcessor implements ItemProcessor<StudentCsv, StudentCsv> {

	@Override
	public StudentCsv process(StudentCsv studentCsv) throws Exception {
		System.out.println("File data processor");
		
//		if(item.getId() == 6) {
//			throw new NullPointerException();
//		}
		studentCsv.setCreatedOn(new Date());
		
		return studentCsv;
	}

}
