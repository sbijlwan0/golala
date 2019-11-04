package com.easygo.web.rest;

import java.util.List;

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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.easygo.domain.Category;
import com.easygo.repository.CategoryRepository;
import com.easygo.service.dto.ResultStatus;

@RestController
@RequestMapping("/api")
public class CategoryResource {
	
	private final Logger log = LoggerFactory.getLogger(CategoryResource.class);
	
	@Autowired
	CategoryRepository categoryRepo;
	
	
	@PostMapping("/category")
	public ResponseEntity<?> addCategory(@Valid @RequestBody Category cat){
		
		log.debug("rest request to add category");
		
		Category result=categoryRepo.save(cat);
		
		return new ResponseEntity<>(new ResultStatus("Success","Category added",result),HttpStatus.CREATED);
		
	}
	
	
	@PutMapping("/category")
	public ResponseEntity<?> updateCategory(@Valid @RequestBody Category cat){
		
		log.debug("rest request to update category");
		
		Category result=categoryRepo.save(cat);
		
		return new ResponseEntity<>(new ResultStatus("Success","Category added",result),HttpStatus.OK);
		
	}
	
	
	@GetMapping("/category/{page}")
	public ResponseEntity<?> getAllCategory(@PathVariable("page")int page){
		
		log.debug("rest request to get all category");
		
		return new ResponseEntity<>(new ResultStatus("Success","Category added",categoryRepo.findAll(PageRequest.of(page, 10))),HttpStatus.OK);
		
	}
	
	@GetMapping("/getTree")
	public ResponseEntity<?> getCategoryTree(){
		
		Category category=categoryRepo.findById("All Category").get();
		
		generateTree(category);
		
		return new ResponseEntity<>(new ResultStatus("success","category fetched",category),HttpStatus.OK);
	}
	
	
	@DeleteMapping("/removeCategory/{id}")
	public ResponseEntity<?> removeCategory(@PathVariable("id")String id){
		
		log.debug("rest request to remove category");
		
		categoryRepo.deleteById(id);
		
		return new ResponseEntity<>(new ResultStatus("Success","Category removed"),HttpStatus.OK);
		
	}
	
	
	public void generateTree(Category cat) {
		
		List<Category>subCat=categoryRepo.findAllByParentCategory(cat.getName());
		for (Category category : subCat) {
			generateTree(category);
		}
		cat.setSubCat(subCat);
	}

}
