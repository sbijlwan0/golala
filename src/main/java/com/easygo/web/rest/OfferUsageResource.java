package com.easygo.web.rest;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.easygo.domain.Coupon;
import com.easygo.domain.User;
import com.easygo.repository.CouponRepository;
import com.easygo.repository.OfferUsageRepository;
import com.easygo.repository.UserRepository;
import com.easygo.security.SecurityUtils;
import com.easygo.service.dto.ResultStatus;


@RestController
@RequestMapping("/api")
public class OfferUsageResource {
	
private final Logger log = LoggerFactory.getLogger(OfferUsageResource.class);
	
	@Autowired
	CouponRepository coupRepo;
	
	@Autowired
	OfferUsageRepository offUsRepo;
	
	@Autowired
	UserRepository userRepo;
	
	@GetMapping("verifyCode/{code}")
	public ResponseEntity<?> verifyCouponCode(@PathVariable("code")String code){
	
		log.debug("rest request to verify code",code);
		
		if(!SecurityUtils.getCurrentUserLogin().isPresent() || SecurityUtils.getCurrentUserLogin().get().equalsIgnoreCase(""))
			return new ResponseEntity<>(new ResultStatus("Error","Login First"),HttpStatus.BAD_REQUEST);
		
		
		try {
		
		User user=userRepo.findOneByLogin(SecurityUtils.getCurrentUserLogin().get()).get();
		
		if(coupRepo.findByCode(code).isPresent()) {
			Coupon coup=coupRepo.findByCode(code).get();
			Instant d = Instant.now();
			if(coup.getCreatedDate().until(d, ChronoUnit.DAYS)<coup.getValidDays()) {
				if(offUsRepo.findOneByOfferIdAndUserId(coup.getId(), user.getId()).isPresent()) {
					if(offUsRepo.findOneByOfferIdAndUserId(coup.getId(), user.getId()).get().getAppliedCount()<coup.getMaxApplyCount())
						return new ResponseEntity<>(new ResultStatus("Success","Coupon Valid",coup),HttpStatus.OK);
					return new ResponseEntity<>(new ResultStatus("Error","Coupon Applied Max. Times"),HttpStatus.BAD_REQUEST);
				}	
				return new ResponseEntity<>(new ResultStatus("Success","Coupon Valid",coup),HttpStatus.OK);
			}
			return new ResponseEntity<>(new ResultStatus("Error","Coupon Expired"),HttpStatus.BAD_REQUEST);
			
		}
		else
			return new ResponseEntity<>(new ResultStatus("Error","Invalid Coupon"),HttpStatus.BAD_REQUEST);
		
		}catch(Exception e) {
			return new ResponseEntity<>(new ResultStatus("Error","Token Expired. Login Again"),HttpStatus.BAD_REQUEST);
		}
		
	}

	
	
}
