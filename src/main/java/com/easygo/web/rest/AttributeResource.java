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

import com.easygo.domain.Attribute;
import com.easygo.repository.AttributeRepository;
import com.easygo.service.dto.ResultStatus;

import io.undertow.util.BadRequestException;



@RestController
@RequestMapping("/api")
public class AttributeResource {
	
	private final Logger log = LoggerFactory.getLogger(AttributeResource.class);
	
	@Autowired
	AttributeRepository attRepo;
	
	@PostMapping("/attribute")
	public ResponseEntity<?> addAttribute(@Valid @RequestBody Attribute attribute) throws BadRequestException{
		
		log.debug("rest request to add attributes");
		
		if(null!=attribute.getId())
			throw new BadRequestException("Id must be null");
		
		Attribute result=attRepo.save(attribute);
		
		return new ResponseEntity<>(new ResultStatus("Success","Attribute created",result),HttpStatus.CREATED);
	}
	
	
	@PutMapping("/attribute")
	public ResponseEntity<?> updateAttribute(@Valid @RequestBody Attribute attribute) throws BadRequestException{
		
		log.debug("rest request to update attributes");
		
		if(null==attribute.getId())
			throw new BadRequestException("Id must not be null");
		
		Attribute result=attRepo.save(attribute);
		
		return new ResponseEntity<>(new ResultStatus("Success","Attribute Updated",result),HttpStatus.OK);
	}
	
	
	@GetMapping("/attribute/{page}")
	public ResponseEntity<?> getAllAttribute(@PathVariable("page")int page){
		
		log.debug("rest request to get all attributes");
		
		return new ResponseEntity<>(new ResultStatus("Success","Attribute Fetched",attRepo.findAll(PageRequest.of(page, 10))),HttpStatus.OK);
	}
	
	
	@GetMapping("/attributeById/{id}")
	public ResponseEntity<?> getAllAttribute(@PathVariable("id")String id) throws BadRequestException{
		
		log.debug("rest request to get all attributes");
		
		if(!attRepo.findById(id).isPresent())
			throw new BadRequestException("Invalid Id");
		
		return new ResponseEntity<>(new ResultStatus("Success","Attribute Fetched",attRepo.findById(id).get()),HttpStatus.OK);
	}
	
	

	@DeleteMapping("/attribute/{id}")
	public ResponseEntity<?> removeAttributeById(@PathVariable("id")String id) throws BadRequestException{
		
		log.debug("rest request to get all attributes");
		
		if(!attRepo.findById(id).isPresent())
			throw new BadRequestException("Invalid Id");
		
		attRepo.deleteById(id);
		
		return new ResponseEntity<>(new ResultStatus("Success","Attribute removed"),HttpStatus.OK);
	}
}
