package com.easygo.domain;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.springframework.data.annotation.Id;

@org.springframework.data.mongodb.core.mapping.Document(collection="favourite")
public class Favourite {
	
	@Id
	private String id;
	
	@NotNull
	private String userId;
	
	private List<Organisation> organisations;

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

	public List<Organisation> getOrganisations() {
		return organisations;
	}

	public void setOrganisations(List<Organisation> organisations) {
		this.organisations = organisations;
	}
	
	

}
