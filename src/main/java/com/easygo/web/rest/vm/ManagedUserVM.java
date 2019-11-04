package com.easygo.web.rest.vm;

import com.easygo.service.dto.BankDetails;
import com.easygo.service.dto.UserDTO;
import javax.validation.constraints.Size;

/**
 * View Model extending the UserDTO, which is meant to be used in the user management UI.
 */
public class ManagedUserVM extends UserDTO {

    public static final int PASSWORD_MIN_LENGTH = 4;

    public static final int PASSWORD_MAX_LENGTH = 100;

//    @Size(min = PASSWORD_MIN_LENGTH, max = PASSWORD_MAX_LENGTH)
//    private String password;
    
    private String gstNo;
	
	private String pan;
	
	private String drivingLic;
	
	private String aadhar;
	
	private BankDetails bank;
	
	private String type="vendor";
	
	
	
	

    public String getDrivingLic() {
		return drivingLic;
	}

	public void setDrivingLic(String drivingLic) {
		this.drivingLic = drivingLic;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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

	public ManagedUserVM() {
        // Empty constructor needed for Jackson.
    }

//    public String getPassword() {
//        return password;
//    }
//
//    public void setPassword(String password) {
//        this.password = password;
//    }

    @Override
    public String toString() {
        return "ManagedUserVM{" +
            "} " + super.toString();
    }
}
