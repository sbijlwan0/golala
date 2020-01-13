package com.easygo.domain;

import java.io.Serializable;
import java.util.TreeMap;

import org.springframework.data.annotation.Id;

@org.springframework.data.mongodb.core.mapping.Document(collection="order_payments")
public class Payment extends AbstractAuditingEntity implements Serializable{

	private static final long serialVersionUID = 1L;

    @Id
    private String id;
    
    private String rootOrderId;
    
    private String userId;
    
    private TreeMap<String,String> params=new TreeMap<String,String>();
    
    private boolean success=false;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getRootOrderId() {
		return rootOrderId;
	}

	public void setRootOrderId(String rootOrderId) {
		this.rootOrderId = rootOrderId;
	}

	public TreeMap<String, String> getParams() {
		return params;
	}

	public void setParams(TreeMap<String, String> params) {
		this.params = params;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	
    
    

}
