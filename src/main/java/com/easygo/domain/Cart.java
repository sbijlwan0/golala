package com.easygo.domain;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.springframework.data.annotation.Id;

import com.easygo.service.dto.ProductDTO;

@org.springframework.data.mongodb.core.mapping.Document(collection="cart")
public class Cart {

	@Id
	private String id;
	
	@NotNull
	private String userId;
	
	private List<ProductDTO> items;

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

	public List<ProductDTO> getItems() {
		return items;
	}

	public void setItems(List<ProductDTO> items) {
		this.items = items;
	}
	
	
	
	
}
