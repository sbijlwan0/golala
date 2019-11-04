package com.easygo.service.dto;

import java.util.List;

import com.easygo.domain.Product;

public class Item {

	private String label;
	
	private List<Product> product;
	
	
	
	

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public List<Product> getProduct() {
		return product;
	}

	public void setProduct(List<Product> product) {
		this.product = product;
	}
	
	
	
	
}
