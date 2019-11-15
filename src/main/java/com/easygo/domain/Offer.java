package com.easygo.domain;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.annotation.Id;

@org.springframework.data.mongodb.core.mapping.Document(collection="offer")
public class Offer extends AbstractAuditingEntity implements Serializable{

	private static final long serialVersionUID = 1L;

    @Id
    private String id;
    
    private String name;
    
    private String code;
    
    private String type;
    
    private String description;
    
    private String discountFrom;
    
    private int validityDays;
    
    private double discountAmount;
    
    private double discountPercent;
    
    private List<String> appliedUser;
    
    private int applicableCount;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDiscountFrom() {
		return discountFrom;
	}

	public void setDiscountFrom(String discountFrom) {
		this.discountFrom = discountFrom;
	}

	public int getValidityDays() {
		return validityDays;
	}

	public void setValidityDays(int validityDays) {
		this.validityDays = validityDays;
	}

	public double getDiscountAmount() {
		return discountAmount;
	}

	public void setDiscountAmount(double discountAmount) {
		this.discountAmount = discountAmount;
	}

	public double getDiscountPercent() {
		return discountPercent;
	}

	public void setDiscountPercent(double discountPercent) {
		this.discountPercent = discountPercent;
	}

	public List<String> getAppliedUser() {
		return appliedUser;
	}

	public void setAppliedUser(List<String> appliedUser) {
		this.appliedUser = appliedUser;
	}

	public int getApplicableCount() {
		return applicableCount;
	}

	public void setApplicableCount(int applicableCount) {
		this.applicableCount = applicableCount;
	}
    
    
 
    

}
