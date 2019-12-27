package com.easygo.web.rest;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

import com.easygo.domain.Review;
import com.easygo.domain.User;
import com.easygo.repository.ReviewRepository;
import com.easygo.repository.UserRepository;
import com.easygo.security.SecurityUtils;
import com.easygo.service.dto.ResultStatus;

@RestController
@RequestMapping("/api")
public class ReviewResource {
	
	private final Logger log = LoggerFactory.getLogger(ReviewResource.class);
	
	@Autowired
	ReviewRepository revRepo;
	
	@Autowired
	UserRepository userRepo;
	
	
	@PostMapping("/review")
	public ResponseEntity<?> addReview(@Valid @RequestBody Review rev){
		
		log.debug("rest request to update Review");
		
		try {
			User user = userRepo.findOneByLogin(SecurityUtils.getCurrentUserLogin().get()).get();
			
			if(!rev.getId().equalsIgnoreCase(user.getId()))
				return new ResponseEntity<>(new ResultStatus("Error","Invalid User"),HttpStatus.BAD_REQUEST);
			
			if(revRepo.findOneByUserIdAndItemIdAndType(rev.getUserId(), rev.getItemId(), rev.getType()).isPresent())
				return new ResponseEntity<>(new ResultStatus("Error","Review by this user already exist"),HttpStatus.BAD_REQUEST);
			
			revRepo.save(rev);
			return new ResponseEntity<>(new ResultStatus("Success","Review Created", rev),HttpStatus.CREATED);	
		}catch(Exception a) {
			return new ResponseEntity<>(new ResultStatus("Error","Please Login"),HttpStatus.BAD_REQUEST);	
		}
		
	}
	
	
	@PutMapping("/review")
	public ResponseEntity<?> updateReview(@Valid @RequestBody Review rev){
		
		log.debug("rest request to update Review");
		
		try {
			User user = userRepo.findOneByLogin(SecurityUtils.getCurrentUserLogin().get()).get();
			
			if(!rev.getId().equalsIgnoreCase(user.getId()))
				return new ResponseEntity<>(new ResultStatus("Error","Invalid User"),HttpStatus.BAD_REQUEST);
			
			if(!revRepo.findOneByUserIdAndItemIdAndType(rev.getUserId(), rev.getItemId(), rev.getType()).isPresent())
				return new ResponseEntity<>(new ResultStatus("Error","Review Not Exist"),HttpStatus.BAD_REQUEST);
			
			revRepo.save(rev);
			return new ResponseEntity<>(new ResultStatus("Success","Review Updated", rev),HttpStatus.OK);	
		}catch(Exception a) {
			return new ResponseEntity<>(new ResultStatus("Error","Please Login"),HttpStatus.BAD_REQUEST);	
		}
		
	}
	
	
	@GetMapping("/review")
	public ResponseEntity<?> getAllReview(){
		
		log.debug("rest request to get Review");
		
			return new ResponseEntity<>(new ResultStatus("Success","Review Fetched", revRepo.findAll()),HttpStatus.OK);	
		
	}
	
	@GetMapping("/reviewById/{id}")
	public ResponseEntity<?> getReviewById(@PathVariable("id")String id){
		
		log.debug("rest request to get Review");
		
			return new ResponseEntity<>(new ResultStatus("Success","Review Fetched", revRepo.findAll()),HttpStatus.OK);	
		
	}
	
	
	@GetMapping("/reviewByItemIdAndType/{id}/{type}")
	public ResponseEntity<?> getReviewByItemId(@PathVariable("id")String id,@PathVariable("type")String type){
		
		log.debug("rest request to get Review");
		
		try {
			User user = userRepo.findOneByLogin(SecurityUtils.getCurrentUserLogin().get()).get();
			
			return new ResponseEntity<>(new ResultStatus("Success","Review Created", revRepo.findOneByUserIdAndItemIdAndType(user.getId(), id, type).get()),HttpStatus.OK);	
		}catch(Exception a) {
			return new ResponseEntity<>(new ResultStatus("Error","Please Login"),HttpStatus.BAD_REQUEST);	
		}
		
	}
	
	@DeleteMapping("/review/{id}")
	public ResponseEntity<?> removeReviewById(@PathVariable("id")String id){
		
		log.debug("rest request to remove Review");
		
		revRepo.deleteById(id);
		
			return new ResponseEntity<>(new ResultStatus("Success","Review Removed"),HttpStatus.OK);	
		
	}

}
