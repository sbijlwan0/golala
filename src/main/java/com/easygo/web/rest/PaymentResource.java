package com.easygo.web.rest;

import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.easygo.domain.Payment;
import com.easygo.repository.PaymentRepository;
import com.easygo.service.dto.ResultStatus;
//import com.munim.domain.Payment;

@RestController
@RequestMapping("/api")
public class PaymentResource {

	private final Logger log = LoggerFactory.getLogger(PaymentResource.class);
	
	@Autowired
	PaymentRepository paymentRepo;
	
	@Autowired
	MongoTemplate mongoTemplate;
	
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
		
		Sort sort = new Sort(Sort.Direction.DESC,"created_date");
		
		return new ResponseEntity<>(new ResultStatus("Success","paymentsFetched",paymentRepo.findAllByRootOrderId(id,sort)),HttpStatus.OK);
	}
	
	
//	@Scheduled(cron = "0 0 7 * * ?",zone = "GMT+5:30")
//	public void clearPayments() {
//		log.debug("cron job to clear peding payments");
//		
//		Criteria criteria = new Criteria();
//		
//		criteria.andOperator(Criteria.where("created_date").lt(Instant.now().minusSeconds(86400*2)),//2days
//				Criteria.where("success").is(false));
//		
//		Query query = new Query(criteria);
//		
//		List<Payment> payments = mongoTemplate.find(query, Payment.class, "order_payments");
//		
//		paymentRepo.deleteAll(payments);
//	}
}
