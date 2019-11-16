package com.easygo.web.rest;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.easygo.domain.Coupon;
import com.easygo.repository.CouponRepository;
import com.easygo.service.dto.ResultStatus;

@RestController
@RequestMapping("/api")
public class CouponResource {
	
	private final Logger log = LoggerFactory.getLogger(CouponResource.class);
	
	@Autowired
	CouponRepository coupRepo;
	
	@PostMapping("/coupon")
	public ResponseEntity<?> addCoupon(@Valid @RequestBody Coupon coupon){
		
		log.debug("rest request to add new coupon");
		
		if(null!=coupon.getId())
			return new ResponseEntity<>(new ResultStatus("Error","new coupon must not have an id"),HttpStatus.BAD_REQUEST);
		
		
		if(coupRepo.findByCode(coupon.getCode()).isPresent()) 
			return new ResponseEntity<>(new ResultStatus("Error","Coupon with this code already exist"),HttpStatus.BAD_REQUEST);
		
		Coupon result = coupRepo.save(coupon);
		
		return new ResponseEntity<>(new ResultStatus("Success","Coupon created",result),HttpStatus.CREATED); 
			
	}
	
	
	@PutMapping("/coupon")
	public ResponseEntity<?> updateCoupon(@Valid @RequestBody Coupon coupon){
		
		log.debug("rest request to update coupon");
		
		if(null==coupon.getId())
			return new ResponseEntity<>(new ResultStatus("Error","coupon must have an id"),HttpStatus.BAD_REQUEST);
		
		
		if(!coupRepo.findByCode(coupon.getCode()).isPresent()) 
			return new ResponseEntity<>(new ResultStatus("Error","Invalid Request. Coupon not Found"),HttpStatus.BAD_REQUEST);
		
		Coupon result = coupRepo.save(coupon);
		
		return new ResponseEntity<>(new ResultStatus("Success","Coupon updated",result),HttpStatus.OK); 
			
	}
	
	
	
	@GetMapping("/coupon/{page}")
	public ResponseEntity<?> getAllCoupon(@PathVariable("page")int page){
		
		log.debug("rest request to get all coupon");
		
		Sort sort = new Sort(Sort.Direction.DESC, "Created_Date");
		
		return new ResponseEntity<>(new ResultStatus("Success","Coupon Fetched",coupRepo.findAll(PageRequest.of(page, 10, sort))),HttpStatus.OK); 
			
	}
	
	@GetMapping("/couponById/{id}")
	public ResponseEntity<?> getCouponById(@PathVariable("id")String id){
		
		log.debug("rest request to get coupon by id",id);
		
		return new ResponseEntity<>(new ResultStatus("Success","Coupon Fetched",coupRepo.findById(id).get()),HttpStatus.OK); 
			
	}
	
	
	@GetMapping("/couponByCode/{code}")
	public ResponseEntity<?> getCouponByCode(@PathVariable("code")String code){
		
		log.debug("rest request to get coupon by code",code);
		
		return new ResponseEntity<>(new ResultStatus("Success","Coupon Fetched",coupRepo.findByCode(code).get()),HttpStatus.OK); 
			
	}
	
	
	@DeleteMapping("/coupon/{id}")
	public ResponseEntity<?> removeCouponById(@PathVariable("id")String id){
		
		log.debug("rest request to remove coupon by id",id);
		
		coupRepo.deleteById(id);
		
		return new ResponseEntity<>(new ResultStatus("Success","Coupon Removed"),HttpStatus.OK); 
			
	}

}
