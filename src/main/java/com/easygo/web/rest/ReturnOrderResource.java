package com.easygo.web.rest;

import java.time.Instant;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.easygo.domain.Order;
import com.easygo.domain.Product;
import com.easygo.repository.OrderRepository;
import com.easygo.repository.ProductRepository;
import com.easygo.service.dto.ProductDTO;
import com.easygo.service.dto.ResultStatus;
import com.easygo.service.dto.SubProduct;
import com.easygo.service.util.RandomUtil;

import io.undertow.util.BadRequestException;

@RestController
@RequestMapping("/api")
public class ReturnOrderResource {

	private final Logger log = LoggerFactory.getLogger(ReturnOrderResource.class);

	@Autowired
	OrderRepository orderRepo;

	@Autowired
	ProductRepository proRepo;
	
	
	@PostMapping("/returnOrder")
	public ResponseEntity<?> addReturnOrder(@Valid @RequestBody Order order){
		
		log.debug("add return Order");
		
		order.setReturnOrder(true);
		
		order.setCustomerOtp(RandomUtil.generateOTP());
		
		order.setVendorOtp(RandomUtil.generateOTP());
		
		Order result=orderRepo.save(order);
		
		return new ResponseEntity<>(new ResultStatus("Success","Return Added",result),HttpStatus.CREATED);
		
	}
	
	@PutMapping("updateReturnStatus/{otp}/{status}")
	public ResponseEntity<?> verifyReturnOTP(@PathVariable("otp") String otp, @PathVariable("status") String status, @RequestBody Order order) throws BadRequestException{
		
		switch(status){
			
		case "Picked": if(order.getCustomerOtp().equalsIgnoreCase(otp)) {
			order.setStatus(status);
			updateProduct(order,1);
			break;
			}
		throw new BadRequestException("Invalid OTP");
		
		case "Delivered": if(order.getVendorOtp().equalsIgnoreCase(otp)) {
			order.setStatus(status);
			order.setDeliveryTime(Instant.now());
			updateProduct(order,0);
			break;
		}
		throw new BadRequestException("Invalid OTP");
		
		default: throw new BadRequestException("Invalid Status");
		
		}
		
		Order result = orderRepo.save(order);
		
		return new ResponseEntity<>(new ResultStatus("Success","Status Updated",result),HttpStatus.OK);
	}
	
	@GetMapping("/returnOrder/{page}")
	public ResponseEntity<?> getAllReturnOrder(@PathVariable("page") int page) {

		log.debug("rest request to get All return order.");

		return new ResponseEntity<>(new ResultStatus("Success", "Order Fetched",
				orderRepo.findAllByReturnOrder(true, PageRequest.of(page, 10))), HttpStatus.OK);
	}

	@GetMapping("/returnOrderByUserId/{userId}/{page}")
	public ResponseEntity<?> getReturnOrderByUser(@PathVariable("userId") String userId, @PathVariable("page") int page) {

		log.debug("rest request to get return order by user.");

		return new ResponseEntity<>(new ResultStatus("Success", "Order Fetched",
				orderRepo.findByReturnOrderAndUserId(true, userId, PageRequest.of(page, 10))), HttpStatus.OK);
	}
	
	
	public void updateProduct(Order order,int n) {
		
		Order actualOrder=orderRepo.findById(order.getOrderId()).get();
		
		for(ProductDTO product : order.getItems()) {
			if(n==0) {
			Product pro = proRepo.findById(product.getProductId()).get();
			
			for(SubProduct spro:pro.getSubProduct())
				if(spro.getId()==product.getSubProductId())
					spro.setQuantity(spro.getQuantity()+product.getQuantity());
			proRepo.save(pro);
			}
			for(ProductDTO item :actualOrder.getItems()) {
				
				if(product.getSubProductId()==item.getSubProductId()) {
					if(n==0)
						item.setStatus("Returned");
					else if(n==1)
						item.setStatus("Return in Process");
				}
				
			}
			
			orderRepo.save(actualOrder);
		}
		
	}
}
