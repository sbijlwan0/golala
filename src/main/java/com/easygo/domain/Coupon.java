package com.easygo.domain;

import java.io.Serializable;


import org.springframework.data.annotation.Id;

@org.springframework.data.mongodb.core.mapping.Document(collection="coupon")
public class Coupon extends AbstractAuditingEntity implements Serializable{

	private static final long serialVersionUID = 1L;

    @Id
    private String id;
    
    private String description;
    
    private String code;
    
    private String imageUrl;
    
    private double discountAmount;
    
    private double discountPercent;
    
    private long validDays;
    
    private int maxApplyCount=1;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
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

	public long getValidDays() {
		return validDays;
	}

	public void setValidDays(long validDays) {
		this.validDays = validDays;
	}

	public int getMaxApplyCount() {
		return maxApplyCount;
	}

	public void setMaxApplyCount(int maxApplyCount) {
		this.maxApplyCount = maxApplyCount;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
    
    
    

}
