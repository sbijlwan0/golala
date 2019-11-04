package com.easygo.domain;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.springframework.data.annotation.Id;

import com.easygo.service.dto.Item;

@org.springframework.data.mongodb.core.mapping.Document(collection="menu")
public class Menu {

	@Id
	private String id;
	
	@NotNull
	private String orgId;
	
	private List<Item>itemList;

	
	
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public List<Item> getItemList() {
		return itemList;
	}

	public void setItemList(List<Item> itemList) {
		this.itemList = itemList;
	}


	
	
	
}
