package com.easygo.web.rest;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
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

import com.easygo.domain.AttributeSet;
import com.easygo.repository.AttributeSetRepository;
import com.easygo.service.dto.ResultStatus;

import io.undertow.util.BadRequestException;

@RestController
@RequestMapping("/api")
public class AttributeSetResource {
	
	private final Logger log = LoggerFactory.getLogger(AttributeSetResource.class);
	
	@Autowired
	AttributeSetRepository attRepo;
	
	@PostMapping("/attributeSet")
	public ResponseEntity<?> addAttributeSet(@Valid @RequestBody AttributeSet attributeSet) throws BadRequestException{
		
		log.debug("rest request to add attributeSets");
		
		if(null!=attributeSet.getId())
			throw new BadRequestException("Id must be null");
		
		AttributeSet result=attRepo.save(attributeSet);
		
		return new ResponseEntity<>(new ResultStatus("Success","AttributeSet created",result),HttpStatus.CREATED);
	}
	
	
	@PutMapping("/attributeSet")
	public ResponseEntity<?> updateAttributeSet(@Valid @RequestBody AttributeSet attributeSet) throws BadRequestException{
		
		log.debug("rest request to update attributeSets");
		
		if(null==attributeSet.getId())
			throw new BadRequestException("Id must not be null");
		
		AttributeSet result=attRepo.save(attributeSet);
		
		return new ResponseEntity<>(new ResultStatus("Success","AttributeSet Updated",result),HttpStatus.OK);
	}
	
	
	@GetMapping("/attributeSet/{page}")
	public ResponseEntity<?> getAllAttributeSet(@PathVariable("page")int page){
		
		log.debug("rest request to get all attributeSets");
		
		return new ResponseEntity<>(new ResultStatus("Success","AttributeSet Fetched",attRepo.findAll(PageRequest.of(page, 10))),HttpStatus.OK);
	}
	
	
	@GetMapping("/attributeSetById/{id}")
	public ResponseEntity<?> getAllAttributeSet(@PathVariable("id")String id) throws BadRequestException{
		
		log.debug("rest request to get all attributeSets");
		
		if(!attRepo.findById(id).isPresent())
			throw new BadRequestException("Invalid Id");
		
		return new ResponseEntity<>(new ResultStatus("Success","AttributeSet Fetched",attRepo.findById(id).get()),HttpStatus.OK);
	}
	
	

	@DeleteMapping("/attributeSet/{id}")
	public ResponseEntity<?> removeAttributeSetById(@PathVariable("id")String id) throws BadRequestException{
		
		log.debug("rest request to get all attributeSets");
		
		if(!attRepo.findById(id).isPresent())
			throw new BadRequestException("Invalid Id");
		
		attRepo.deleteById(id);
		
		return new ResponseEntity<>(new ResultStatus("Success","AttributeSet removed"),HttpStatus.OK);
	}
}
