package com.easygo.service.dto;

import java.util.List;

public class ProductDTO {

	private String name;
	
	private List<String> images;
	
	private List<AttributeDTO> attribute;
	
	private String productId;
	
	private double discountPrice;
	
	private double price;
	
	private double quantity;
	
	private int subProductId;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getImages() {
		return images;
	}

	public void setImages(List<String> images) {
		this.images = images;
	}

	public List<AttributeDTO> getAttribute() {
		return attribute;
	}

	public void setAttribute(List<AttributeDTO> attribute) {
		this.attribute = attribute;
	}

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public double getDiscountPrice() {
		return discountPrice;
	}

	public void setDiscountPrice(double discountPrice) {
		this.discountPrice = discountPrice;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public double getQuantity() {
		return quantity;
	}

	public void setQuantity(double quantity) {
		this.quantity = quantity;
	}

	public int getSubProductId() {
		return subProductId;
	}

	public void setSubProductId(int subProductId) {
		this.subProductId = subProductId;
	}
	
	
	
	
}
