package com.easygo.web.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.easygo.repository.PaymentRepository;
import com.easygo.service.dto.ResultStatus;

@RestController
@RequestMapping("/api")
public class PaymentResource {

	private final Logger log = LoggerFactory.getLogger(PaymentResource.class);
	
	@Autowired
	PaymentRepository paymentRepo;
	
	@GetMapping("/payment")
	public ResponseEntity<?> getAllPayments(){
		log.debug("rest request to get all payments");
		
		return new ResponseEntity<>(new ResultStatus("Success","paymentsFetched",paymentRepo.findAll()),HttpStatus.OK);
	}
	
	
	@GetMapping("/payment/{id}")
	public ResponseEntity<?> getPaymentById(@PathVariable("id")String id){
		log.debug("rest request to get payment by id",id);
		
		return new ResponseEntity<>(new ResultStatus("Success","paymentsFetched",paymentRepo.findById(id).get()),HttpStatus.OK);
	}
	
	
	@GetMapping("/paymentsByUserId/{id}")
	public ResponseEntity<?> getPaymentsByUserId(@PathVariable("id")String id){
		log.debug("rest request to get payment by id",id);
		
		return new ResponseEntity<>(new ResultStatus("Success","paymentsFetched",paymentRepo.findByUserId(id)),HttpStatus.OK);
	}
	
	
	@GetMapping("/paymentsByRootOrderId/{id}")
	public ResponseEntity<?> getPaymentsByRootOrderId(@PathVariable("id")String id){
		log.debug("rest request to get payment by id",id);
		
		return new ResponseEntity<>(new ResultStatus("Success","paymentsFetched",paymentRepo.findAllByRootOrderId(id)),HttpStatus.OK);
	}
}
