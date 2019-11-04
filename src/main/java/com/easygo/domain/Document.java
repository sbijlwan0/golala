package com.easygo.domain;

import org.springframework.data.annotation.Id;

import com.easygo.service.dto.BankDetails;

@org.springframework.data.mongodb.core.mapping.Document(collection = "docs")
public class Document {
	
	@Id
	private String id;
	
	private String userId;
	
	private String gstNo;
	
	private String pan;
	
	private String drivingLic;
	
	private String aadhar;
	
	private BankDetails bank;
	
	
	

	public String getDrivingLic() {
		return drivingLic;
	}

	public void setDrivingLic(String drivingLic) {
		this.drivingLic = drivingLic;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getGstNo() {
		return gstNo;
	}

	public void setGstNo(String gstNo) {
		this.gstNo = gstNo;
	}

	public String getPan() {
		return pan;
	}

	public void setPan(String pan) {
		this.pan = pan;
	}

	public String getAadhar() {
		return aadhar;
	}

	public void setAadhar(String aadhar) {
		this.aadhar = aadhar;
	}

	public BankDetails getBank() {
		return bank;
	}

	public void setBank(BankDetails bank) {
		this.bank = bank;
	}
	
	
	

}
