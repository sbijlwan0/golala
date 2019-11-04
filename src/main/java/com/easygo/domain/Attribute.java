package com.easygo.domain;

import org.springframework.data.annotation.Id;

@org.springframework.data.mongodb.core.mapping.Document(collection="attrinute")
public class Attribute {
	
	@Id
	private String id;
	
	private String name;
	
	private String inputType;
	
	private String viewType;
	
	

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

	public String getInputType() {
		return inputType;
	}

	public void setInputType(String inputType) {
		this.inputType = inputType;
	}

	public String getViewType() {
		return viewType;
	}

	public void setViewType(String viewType) {
		this.viewType = viewType;
	}

	@Override
	public String toString() {
		return "Attribute [id=" + id + ", name=" + name + ", inputType=" + inputType + ", viewType=" + viewType + "]";
	}
	
	
	
	

}
