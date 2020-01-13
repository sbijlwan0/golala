package com.easygo.domain;

import org.springframework.data.annotation.Id;

@org.springframework.data.mongodb.core.mapping.Document("sequence")
public class SequenceGenerator {
	
	@Id
	private String id;
	
	private String sequence;
	
	private String type;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSequence() {
		return sequence;
	}

	public void setSequence(String sequence) {
		this.sequence = sequence;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	
	

}
