package com.easygo.domain;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;

import com.easygo.service.dto.AttributeDTO;
import com.easygo.service.dto.SubProduct;

@org.springframework.data.mongodb.core.mapping.Document(collection="products")
public class Product extends AbstractAuditingEntity implements Serializable{

	private static final long serialVersionUID = 1L;

    @Id
    private String id;
    
    private String sku;
    
    @NotNull
    private String organisationId;
    
    @DBRef
    private Organisation organisation;
    
    private String name;
    
    private List<String> category;
    
    private String shortDescription;
    
    private String description;
    
    private List<SubProduct> subProduct;
    
    private boolean active=true;
    
    private String image;
    
    private double rating;
    
    private double tax;
    
    
    
    
    

	public String getSku() {
		return sku;
	}

	public void setSku(String sku) {
		this.sku = sku;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getOrganisationId() {
		return organisationId;
	}

	public void setOrganisationId(String organisationId) {
		this.organisationId = organisationId;
	}

	public Organisation getOrganisation() {
		return organisation;
	}

	public void setOrganisation(Organisation organisation) {
		this.organisation = organisation;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getCategory() {
		return category;
	}

	public void setCategory(List<String> category) {
		this.category = category;
	}

	public String getShortDescription() {
		return shortDescription;
	}

	public void setShortDescription(String shortDescription) {
		this.shortDescription = shortDescription;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<SubProduct> getSubProduct() {
		return subProduct;
	}

	public void setSubProduct(List<SubProduct> subProduct) {
		this.subProduct = subProduct;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public double getRating() {
		return rating;
	}

	public void setRating(double rating) {
		this.rating = rating;
	}

	public double getTax() {
		return tax;
	}

	public void setTax(double tax) {
		this.tax = tax;
	}
    
    
    

}
