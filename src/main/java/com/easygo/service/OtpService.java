package com.easygo.service;

import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;




@Service
public class OtpService {
	
	String signupMessage="[#] Your OTP is ##OTP## for login in EasyGo.";
	String loginMessage="[#] Your OTP is ##OTP## to change password of your account in EasyGo.";
	String sender="EASYGO";
	
	@Value("${message.key}")
    private String authKey;
	
	@Value("${message.api}")
    private String api;
	
	private final Logger log = LoggerFactory.getLogger(OtpService.class);
	
		@Async
	    public void sendOtp(String mobile,int n) throws UnsupportedEncodingException {
	    	
			
	    	log.debug("Sending Otp to : " +mobile);
	    	
	    	RestTemplate restTemplate = new RestTemplate();
	      	HttpHeaders headers = new HttpHeaders();
	      	headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
	      	
	      	MultiValueMap<String, String> map= new LinkedMultiValueMap<String, String>();
	      	if(n==0)
	    	map.add("message", signupMessage);
	      	else
	      		map.add("message", loginMessage);
	    	map.add("sender", sender);
	    	map.add("mobile", mobile);
	    	
	    	HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);

	      	ResponseEntity<String> response = restTemplate.postForEntity(api+"sendotp.php?authkey="+authKey, request , String.class );
	      	System.out.println("res ==>"+response.toString());
	      		        
	    }
	    
	    
		@Async
	    public void resendOtp(String mobile) throws UnsupportedEncodingException {
	    	log.debug("Resending Otp to ", mobile);
	    	RestTemplate restTemplate = new RestTemplate();
	      	HttpHeaders headers = new HttpHeaders();
	      	headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
	      	
	      	MultiValueMap<String, String> map= new LinkedMultiValueMap<String, String>();
	    	map.add("mobile", mobile);
	    	map.add("retrytype", "text");
	    	
	    	HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);

	      	ResponseEntity<String> response = restTemplate.postForEntity(api+"retryotp.php?authkey="+authKey, request , String.class );
	      	System.out.println("res ==>"+response.toString());
	      		        
	    } 
	    
	    
		
	    public String verifyOtp( String mobile, String otp) throws UnsupportedEncodingException {
	    	log.debug("verifying Otp on ", mobile);
	    	RestTemplate restTemplate = new RestTemplate();
	      	HttpHeaders headers = new HttpHeaders();
	      	headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
	      	
	      	MultiValueMap<String, String> map= new LinkedMultiValueMap<String, String>();
	      	map.add("mobile", mobile);
	    	map.add("otp", otp);
	    	//map.add("mobile", userDTO.getMobileNo());
	    	

	    	HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);

	      	ResponseEntity<String> response = restTemplate.postForEntity(api+"verifyRequestOTP.php?authkey="+authKey, request , String.class );
	      	System.out.println("res ==>"+response.getStatusCode());
	      	return response.getBody().toString();
	      	
	      		        
	    } 
	    
	    
}
