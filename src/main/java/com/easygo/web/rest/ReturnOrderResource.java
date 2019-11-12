package com.easygo.web.rest;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.easygo.domain.Order;
import com.easygo.repository.OrderRepository;
import com.easygo.repository.ProductRepository;
import com.easygo.service.dto.ResultStatus;

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
		Order result=orderRepo.save(order);
		
		return new ResponseEntity<>(new ResultStatus("Success","Return Added",result),HttpStatus.CREATED);
		
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
}
