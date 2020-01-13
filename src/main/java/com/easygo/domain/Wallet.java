package com.easygo.domain;

import java.io.Serializable;
import java.time.Instant;

import org.springframework.data.annotation.Id;

@org.springframework.data.mongodb.core.mapping.Document(collection="wallet")
public class Wallet extends AbstractAuditingEntity implements Serializable{

	private static final long serialVersionUID = 1L;

    @Id
    private String id;
    
    private String vendorId;
    
    private double amount=0;
    
    private double lastPaidAmount;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getVendorId() {
		return vendorId;
	}

	public void setVendorId(String vendorId) {
		this.vendorId = vendorId;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public double getLastPaidAmount() {
		return lastPaidAmount;
	}

	public void setLastPaidAmount(double lastPaidAmount) {
		this.lastPaidAmount = lastPaidAmount;
	}
    
    
}
