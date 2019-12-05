package com.easygo.service;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.easygo.domain.Order;



@Service
public class PushService {


	
	@Value("${fcm.key}")
    private String authKey;
	
	@Value("${fcm.api}")
    private String api;
	
	private String title="Golala";
	
	private final Logger log = LoggerFactory.getLogger(PushService.class);
	
	@Async
	public  void sendNotificationToSingle(String token){	
		log.debug("sending push to single");
		
		try {
				
				
				 RestTemplate restTemplate = new RestTemplate();
				 
				// MultiValueMap<String, String> headers = new MultiValueMap<String, String>();
				 HttpHeaders requestHeaders = new HttpHeaders();
				 requestHeaders.set("Authorization", "key="+authKey);
				 requestHeaders.set("Content-Type", "application/json");
//				 
			
				 restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
				
				 HashMap<String, String> data = new HashMap<String, String>();
				 data.put("title", title);
				 data.put("body", "Hi Everyone");
				 
					 HashMap<String, Object> body = new HashMap<String, Object>();
					 body.put("notification", data);
					 body.put("to", token);
					 body.put("priority", "high");
					 
					 HashMap<String, Object> headers = new HashMap<String, Object>();
					 headers.put("apns-collapse-id", "123");
					 
					 HashMap<String, Object> apns = new HashMap<String, Object>();
					 apns.put("headers", headers);
					 body.put("apns", apns);

					 HttpEntity<?> request = new HttpEntity<Object>(body,requestHeaders);
					 
		  	         String person= restTemplate.postForObject(api,request,String.class);
		
		  	         System.out.println(person);
			} catch (Exception e) {
				e.printStackTrace();
				// TODO: handle exception
			}
		
			
		}
	
	
	@Async
	public  void sendNotificationToMultiple(List<String> tokens){		
		
		try {
				
				 RestTemplate restTemplate = new RestTemplate();
				 
				// MultiValueMap<String, String> headers = new MultiValueMap<String, String>();
				 HttpHeaders requestHeaders = new HttpHeaders();
				 requestHeaders.set("Authorization", "key="+authKey);
				 requestHeaders.set("Content-Type", "application/json");
//			
				 restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
				
				 HashMap<String, String> data = new HashMap<String, String>();
				 data.put("title", title);
				 data.put("body", "Hi Everyone");
				 data.put("notificationId", String.valueOf(RandomUtils.nextInt()));
				 
					 HashMap<String, Object> body = new HashMap<String, Object>();
					 body.put("notification", data);
					 body.put("data", data);
					 body.put("registration_ids", tokens);
					 body.put("priority", "high");
					 
					 HashMap<String, Object> headers = new HashMap<String, Object>();
					 headers.put("apns-collapse-id", "123");
					 
					 HashMap<String, Object> apns = new HashMap<String, Object>();
					 apns.put("headers", headers);
					 body.put("apns", apns);

					 HttpEntity<?> request = new HttpEntity<Object>(body,requestHeaders);
					 
		  	         String person= restTemplate.postForObject(api,request,String.class);
		
		  	         System.out.println(person);
			} catch (Exception e) {
				e.printStackTrace();
				// TODO: handle exception
			}
		
			
		}
	
	@Async
	public  void sendOrderPlacedPush(List<String> tokens,Order order){		
		
		try {
				
				 RestTemplate restTemplate = new RestTemplate();
				 
				// MultiValueMap<String, String> headers = new MultiValueMap<String, String>();
				 HttpHeaders requestHeaders = new HttpHeaders();
				 requestHeaders.set("Authorization", "key="+authKey);
				 requestHeaders.set("Content-Type", "application/json");
//			
				 restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
				
				 HashMap<String, String> data = new HashMap<String, String>();
				 data.put("title", title+"  - New Message:");
				 data.put("body", "Order with order id-"+order.getId()+" for "+order.getItems().get(0).getName()+" has been placed.");
				 data.put("notificationId", String.valueOf(RandomUtils.nextInt()));
				 
					 HashMap<String, Object> body = new HashMap<String, Object>();
					 body.put("notification", data);
					 body.put("data", data);
					 body.put("registration_ids", tokens);
					 body.put("priority", "high");
					 
					 HashMap<String, Object> headers = new HashMap<String, Object>();
					 headers.put("apns-collapse-id", "123");
					 
					 HashMap<String, Object> apns = new HashMap<String, Object>();
					 apns.put("headers", headers);
					 body.put("apns", apns);

					 HttpEntity<?> request = new HttpEntity<Object>(body,requestHeaders);
					 
		  	         String person= restTemplate.postForObject(api,request,String.class);
		
		  	         System.out.println(person);
			} catch (Exception e) {
				e.printStackTrace();
				// TODO: handle exception
			}
		
			
		}
	
	
	
	@Async
	public  void sendOrderPickedPush(List<String> tokens,Order order){		
		
		try {
				
				 RestTemplate restTemplate = new RestTemplate();
				 
				// MultiValueMap<String, String> headers = new MultiValueMap<String, String>();
				 HttpHeaders requestHeaders = new HttpHeaders();
				 requestHeaders.set("Authorization", "key="+authKey);
				 requestHeaders.set("Content-Type", "application/json");
//			
				 restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
				
				 HashMap<String, String> data = new HashMap<String, String>();
				 data.put("title", title+"  - New Message:");
				 data.put("body", "Otp for Order with order id-"+order.getId()+" for "+order.getItems().get(0).getName()+" has been verified Successfully.");
				 data.put("notificationId", String.valueOf(RandomUtils.nextInt()));
				 
					 HashMap<String, Object> body = new HashMap<String, Object>();
					 body.put("notification", data);
					 body.put("data", data);
					 body.put("registration_ids", tokens);
					 body.put("priority", "high");
					 
					 HashMap<String, Object> headers = new HashMap<String, Object>();
					 headers.put("apns-collapse-id", "123");
					 
					 HashMap<String, Object> apns = new HashMap<String, Object>();
					 apns.put("headers", headers);
					 body.put("apns", apns);

					 HttpEntity<?> request = new HttpEntity<Object>(body,requestHeaders);
					 
		  	         String person= restTemplate.postForObject(api,request,String.class);
		
		  	         System.out.println(person);
			} catch (Exception e) {
				e.printStackTrace();
				// TODO: handle exception
			}
		
			
		}

	
	
	@Async
	public  void sendNotification(List<String> tokens, String msg){		
		
		try {
				
				 RestTemplate restTemplate = new RestTemplate();
				 
				// MultiValueMap<String, String> headers = new MultiValueMap<String, String>();
				 HttpHeaders requestHeaders = new HttpHeaders();
				 requestHeaders.set("Authorization", "key="+authKey);
				 requestHeaders.set("Content-Type", "application/json");
//			
				 restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
				
				 HashMap<String, String> data = new HashMap<String, String>();
				 data.put("title", title+"  - New Message:");
				 data.put("body", msg);
				 data.put("notificationId", String.valueOf(RandomUtils.nextInt()));
				 
					 HashMap<String, Object> body = new HashMap<String, Object>();
					 body.put("notification", data);
					 body.put("data", data);
					 body.put("registration_ids", tokens);
					 body.put("priority", "high");
					 
					 HashMap<String, Object> headers = new HashMap<String, Object>();
					 headers.put("apns-collapse-id", "123");
					 
					 HashMap<String, Object> apns = new HashMap<String, Object>();
					 apns.put("headers", headers);
					 body.put("apns", apns);

					 HttpEntity<?> request = new HttpEntity<Object>(body,requestHeaders);
					 
		  	         String person= restTemplate.postForObject(api,request,String.class);
		
		  	         System.out.println(person);
			} catch (Exception e) {
				e.printStackTrace();
				// TODO: handle exception
			}
		
			
		}


	
}
