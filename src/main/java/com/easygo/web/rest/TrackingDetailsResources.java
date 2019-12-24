package com.easygo.web.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.easygo.domain.Order;
import com.easygo.domain.TrackingDetails;
import com.easygo.domain.User;
import com.easygo.repository.AuthorityRepository;
import com.easygo.repository.TrackingDetailsRepository;
import com.easygo.repository.UserRepository;
import com.easygo.security.AuthoritiesConstants;
import com.easygo.security.SecurityUtils;
import com.easygo.service.dto.ResultStatus;

@RestController
@RequestMapping("/api")
public class TrackingDetailsResources {
	
	@Autowired
	UserRepository userRepo;
	
	@Autowired
	TrackingDetailsRepository trackRepo;
	
	@Autowired
	AuthorityRepository authRepo;
	
	
	@PostMapping("/updateLocation")
	public ResponseEntity<?> updateLocation(@RequestBody GeoJsonPoint liveLocation){
		
		try {
			User user=userRepo.findOneByLogin(SecurityUtils.getCurrentUserLogin().get()).get();
			
			if(!user.getAuthorities().contains(authRepo.findById(AuthoritiesConstants.DELIVERER).get()))
				return new ResponseEntity<>(new ResultStatus("Error", "You are not a golala driver"), HttpStatus.BAD_REQUEST);
			
			TrackingDetails track=new TrackingDetails();
			
			if(trackRepo.findOneByDriverId(user.getId()).isPresent()) {
				
				track=trackRepo.findOneByDriverId(user.getId()).get();
				track.setLiveLocation(liveLocation);
				
			}else {
				
				track.setDriverId(user.getId());
				track.setLiveLocation(liveLocation);
				
			}
			trackRepo.save(track);
			return new ResponseEntity<>(new ResultStatus("Success", "Location Updated",track), HttpStatus.OK);
			
		}catch(Exception e){
			return new ResponseEntity<>(new ResultStatus("Error", "Please Login"), HttpStatus.BAD_REQUEST);
		}
		
	}
	
	
	@GetMapping("/getTrackingLocation/{driverId}")
	public ResponseEntity<?> getTrackingLocation(@PathVariable("driverId")String driverId){
		
		if(!userRepo.findById(driverId).isPresent())
			return new ResponseEntity<>(new ResultStatus("Error", "User Not Found"), HttpStatus.BAD_REQUEST);
			
		User user=userRepo.findById(driverId).get();
		
		if(!user.getAuthorities().contains(authRepo.findById(AuthoritiesConstants.DELIVERER).get()))
			return new ResponseEntity<>(new ResultStatus("Error", "this order does not have a driver yet"), HttpStatus.BAD_REQUEST);
		
		
		return new ResponseEntity<>(new ResultStatus("Success", "Location Fetched",trackRepo.findOneByDriverId(driverId).get()), HttpStatus.OK);
	}

}
