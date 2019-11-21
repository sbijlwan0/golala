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

import com.easygo.domain.AddressDTO;
import com.easygo.repository.AddressRepository;
import com.easygo.service.dto.ResultStatus;

import io.undertow.util.BadRequestException;

@RestController
@RequestMapping("/api")
public class AddressResource {
	
	private final Logger log = LoggerFactory.getLogger(AddressResource.class);
	
	@Autowired
	AddressRepository addRepo;
	
	@PostMapping("/address")
	public ResponseEntity<?> createAddress(@Valid @RequestBody AddressDTO addr) throws BadRequestException{
		
		log.debug("rest request to add address");
		
		if(null!=addr.getId())
			throw new BadRequestException("New Address cannot already have an id.");
		
		AddressDTO result=addRepo.save(addr);
		
		return new ResponseEntity<>(new ResultStatus("Success","Address added",result),HttpStatus.CREATED);
	}
	
	
	@PutMapping("/address")
	public ResponseEntity<?> updateAddress(@Valid @RequestBody AddressDTO addr) throws BadRequestException{
		
		log.debug("rest request to update address");
		
		if(null==addr.getId())
			throw new BadRequestException("New Address must have an id.");
		
		AddressDTO result=addRepo.save(addr);
		
		return new ResponseEntity<>(new ResultStatus("Success","Address updated",result),HttpStatus.OK);
	}
	
	
	@GetMapping("/address")
	public ResponseEntity<?> getAllAddress() throws BadRequestException{
		
		log.debug("rest request to get all address");
		
		return new ResponseEntity<>(new ResultStatus("Success","Address Fetched",addRepo.findAll()),HttpStatus.OK);
	}
	
	
	@GetMapping("/address/{id}")
	public ResponseEntity<?> getAddressById(@PathVariable("id") String id) throws BadRequestException{
		
		log.debug("rest request to get all address");
		
		if(!addRepo.findById(id).isPresent())
			throw new BadRequestException("address with this id is not present.");
		
		return new ResponseEntity<>(new ResultStatus("Success","Address Fetched",addRepo.findAll()),HttpStatus.OK);
	}
	
	
	@GetMapping("/addressByUserId/{userId}")
	public ResponseEntity<?> getAddressByUserId(@PathVariable("userId") String userId) throws BadRequestException{
		
		log.debug("rest request to get all address by user id");
		
		return new ResponseEntity<>(new ResultStatus("Success","Address Fetched",addRepo.findAllByUserId(userId)),HttpStatus.OK);
	}
	
	
	@DeleteMapping("/address/{id}")
	public ResponseEntity<?> removeAddressById(@PathVariable("id") String id) throws BadRequestException{
		
		log.debug("rest request to get all address");
		
		addRepo.deleteById(id);
		
		return new ResponseEntity<>(new ResultStatus("Success","Address removed"),HttpStatus.OK);
	}

}
