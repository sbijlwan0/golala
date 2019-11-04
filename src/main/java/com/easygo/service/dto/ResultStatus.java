package com.easygo.service.dto;

public class ResultStatus {

	private String status;
	
	private String message;
	
	private Object data;

	public ResultStatus() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	

	public ResultStatus(String status) {
		super();
		this.setStatus(status);
	}



	public ResultStatus(String status, String message) {
		super();
		this.setStatus(status);
		this.setMessage(message);
		
	}



	public ResultStatus(String status, String message, Object data) {
		super();
		this.setStatus(status);
		this.setMessage(message);
		this.setData(data);
	}



	public String getStatus() {
		return status;
	}



	public void setStatus(String status) {
		this.status = status;
	}



	public String getMessage() {
		return message;
	}



	public void setMessage(String message) {
		this.message = message;
	}



	public Object getData() {
		return data;
	}



	public void setData(Object data) {
		this.data = data;
	}
	
}
