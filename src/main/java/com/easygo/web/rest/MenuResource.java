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

import com.easygo.domain.Menu;
import com.easygo.repository.MenuRepository;
import com.easygo.service.dto.ResultStatus;

import io.undertow.util.BadRequestException;

@RestController
@RequestMapping("/api")
public class MenuResource {
	
	private final Logger log = LoggerFactory.getLogger(MenuResource.class);
	
	@Autowired
	MenuRepository menuRepo;
	
	@PostMapping("/menu")
	public ResponseEntity<?> addMenu(@Valid @RequestBody Menu menu) throws BadRequestException{
		
		log.debug("rest request to create menu");
		
		if(null!=menu.getId())
			throw new BadRequestException("Must not have an id");
		
		Menu result=menuRepo.save(menu);
		
		return new ResponseEntity<>(new ResultStatus("Success","Menu Created",result),HttpStatus.CREATED);
	}
	
	
	@PutMapping("/menu")
	public ResponseEntity<?> updatedMenu(@Valid @RequestBody Menu menu) throws BadRequestException{
		
		log.debug("rest request to update menu");
		
		if(null==menu.getId())
			throw new BadRequestException("Must have an id");
		
		Menu result=menuRepo.save(menu);
		
		return new ResponseEntity<>(new ResultStatus("Success","Menu Updated",result),HttpStatus.OK);
	}
	
	
	@GetMapping("/menu")
	public ResponseEntity<?> getAllMenu(){
		
		log.debug("rest request to get all menu");
		
		return new ResponseEntity<>(new ResultStatus("Success","Menu fetched",menuRepo.findAll()),HttpStatus.OK);
	}
	
	
	@GetMapping("/menuByOrgId/{orgId}")
	public ResponseEntity<?> getMenuByOrgId(@PathVariable("orgId")String orgId) throws BadRequestException{
		
		log.debug("rest request to get menu by org id");
		
		if(!menuRepo.findByOrgId(orgId).isPresent())
			throw new BadRequestException("Menu Not found");
		
		return new ResponseEntity<>(new ResultStatus("Success","Menu fetched",menuRepo.findByOrgId(orgId).get()),HttpStatus.OK);
	}
	
	
	@DeleteMapping("/menu/{id}")
	public ResponseEntity<?> removeMenuById(@PathVariable("id")String id) throws BadRequestException{
		
		log.debug("rest request to remove menu by id");
		
		if(!menuRepo.findById(id).isPresent())
			throw new BadRequestException("Menu Not found");
		
		menuRepo.deleteById(id);
		
		return new ResponseEntity<>(new ResultStatus("Success","Menu removed"),HttpStatus.OK);
	}

}
