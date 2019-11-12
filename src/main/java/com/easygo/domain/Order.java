package com.easygo.domain;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;

import com.easygo.service.dto.ProductDTO;

@org.springframework.data.mongodb.core.mapping.Document(collection="order")
public class Order extends AbstractAuditingEntity implements Serializable{

	private static final long serialVersionUID = 1L;

    @Id
    private String id;
    
    private String orderId;
    
    @NotNull
    private String userId;
    
    private String delivererId;
    
    private List<ProductDTO> items;
    
    private double price;
    
    private String status="processing";
    
    private String vendorOtp;
    
    private String customerOtp;
    
    private boolean returnOrder=false;
    
    private Instant deliveryTime;
    
    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    private GeoJsonPoint location;
    
    private Address deliveryAddress;
    
    private Address billingAddress;
    
    

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getVendorOtp() {
		return vendorOtp;
	}

	public void setVendorOtp(String vendorOtp) {
		this.vendorOtp = vendorOtp;
	}

	public String getCustomerOtp() {
		return customerOtp;
	}

	public void setCustomerOtp(String customerOtp) {
		this.customerOtp = customerOtp;
	}

	public boolean isReturnOrder() {
		return returnOrder;
	}

	public void setReturnOrder(boolean returnOrder) {
		this.returnOrder = returnOrder;
	}

	public String getDelivererId() {
		return delivererId;
	}

	public void setDelivererId(String delivererId) {
		this.delivererId = delivererId;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	

	public Instant getDeliveryTime() {
		return deliveryTime;
	}

	public void setDeliveryTime(Instant deliveryTime) {
		this.deliveryTime = deliveryTime;
	}

	public GeoJsonPoint getLocation() {
		return location;
	}

	public void setLocation(GeoJsonPoint location) {
		this.location = location;
	}

	public Address getDeliveryAddress() {
		return deliveryAddress;
	}

	public void setDeliveryAddress(Address deliveryAddress) {
		this.deliveryAddress = deliveryAddress;
	}

	public Address getBillingAddress() {
		return billingAddress;
	}

	public void setBillingAddress(Address billingAddress) {
		this.billingAddress = billingAddress;
	}
    
    
    

}
