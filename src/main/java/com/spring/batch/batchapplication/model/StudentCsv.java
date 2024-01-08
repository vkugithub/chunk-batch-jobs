package com.spring.batch.batchapplication.model;

import lombok.Data;

import java.util.Date;

@Data
public class StudentCsv {

	private Long id;

	private String firstName;

	private String lastName;

	private String email;

	private Date createdOn;

	@Override
	public String toString() {
		return "" + id + "," + firstName + "," + lastName + "," + email + "";
	}

}
