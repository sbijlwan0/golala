package com.easygo.domain;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;

@org.springframework.data.mongodb.core.mapping.Document(collection="category")
public class Category {
	
	@Id
	@NotNull
	private String name;
	
	private String image;
	
	private String parentCategory;
	
	@Transient
	private List<Category> subCat; 
	

	
	
	
	

	public String getParentCategory() {
		return parentCategory;
	}

	public void setParentCategory(String parentCategory) {
		this.parentCategory = parentCategory;
	}

	public List<Category> getSubCat() {
		return subCat;
	}

	public void setSubCat(List<Category> subCat) {
		this.subCat = subCat;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	

}
