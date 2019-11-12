package com.easygo.web.rest;

import java.util.ArrayList;
import java.util.List;

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

import com.easygo.domain.Cart;
import com.easygo.domain.User;
import com.easygo.repository.CartRepository;
import com.easygo.repository.UserRepository;
import com.easygo.security.SecurityUtils;
import com.easygo.service.dto.ProductDTO;
import com.easygo.service.dto.ResultStatus;

import io.undertow.util.BadRequestException;

@RestController
@RequestMapping("/api")
public class CartResource {

	private final Logger log = LoggerFactory.getLogger(CartResource.class);

	@Autowired
	UserRepository userRepo;
	
	@Autowired
	CartRepository cartRepo;

	@PostMapping("/cart")
	public ResponseEntity<?> createCart(@Valid @RequestBody ProductDTO product) throws BadRequestException {

		log.debug("rest request to add cart");

		if(!SecurityUtils.getCurrentUserLogin().isPresent() || SecurityUtils.getCurrentUserLogin().get().equalsIgnoreCase(""))
			return new ResponseEntity<>(new ResultStatus("Error","Login First"),HttpStatus.BAD_REQUEST);
		
		
		try {
		
		User user=userRepo.findOneByLogin(SecurityUtils.getCurrentUserLogin().get()).get();
		
		Cart cart=new Cart();
		
		if(cartRepo.findByUserId(user.getId()).isPresent()) {
			cart=cartRepo.findByUserId(user.getId()).get();
			if(null==cart.getItems()) {
			List<ProductDTO> pros=new ArrayList<ProductDTO>();
			pros.add(product);
			cart.setItems(pros);}
			else
				cart.getItems().add(product);
		}
			
		else {
			List<ProductDTO> pros=new ArrayList<ProductDTO>();
			cart.setUserId(user.getId());
			pros.add(product);
			cart.setItems(pros);
		}
		
			
		

		Cart result = cartRepo.save(cart);

		return new ResponseEntity<>(new ResultStatus("Success", "Added to cart", result), HttpStatus.OK);
		
		}catch(Exception e) {
			return new ResponseEntity<>(new ResultStatus("Error","Token Expired. Login Again"),HttpStatus.BAD_REQUEST);
		}
		
		
	}

	@PutMapping("/cart")
	public ResponseEntity<?> updateCart(@Valid @RequestBody Cart cart) throws BadRequestException {

		log.debug("rest request to add cart");

		if (null == cart.getId())
			throw new BadRequestException("id must not be null");

		if (!cartRepo.findByUserId(cart.getUserId()).isPresent())
			throw new BadRequestException("cart for this user does not exist.");

		Cart result = cartRepo.save(cart);

		return new ResponseEntity<>(new ResultStatus("Success", "Cart Updated", result), HttpStatus.OK);
	}

	@GetMapping("/cartById/{id}")
	public ResponseEntity<?> getAllCart(@PathVariable("id") String id) throws BadRequestException {

		log.debug("rest request to get cart by id");

		if (!cartRepo.findById(id).isPresent())
			throw new BadRequestException("Invalid Id");

		return new ResponseEntity<>(new ResultStatus("Success", "Cart Fetched", cartRepo.findById(id).get()),
				HttpStatus.OK);
	}

	@GetMapping("/cartByUserId/{userId}")
	public ResponseEntity<?> getCartByUserId(@PathVariable("userId") String userId) throws BadRequestException {

		log.debug("rest request to get cart by user id");

		if (!cartRepo.findByUserId(userId).isPresent()) {
			Cart cart = new Cart();
			cart.setUserId(userId);
			Cart result = cartRepo.save(cart);
			return new ResponseEntity<>(new ResultStatus("Success", "Cart Fetched", result), HttpStatus.OK);
		}

		return new ResponseEntity<>(new ResultStatus("Success", "Cart Fetched", cartRepo.findByUserId(userId).get()),
				HttpStatus.OK);
	}

	@DeleteMapping("/cart/{cartId}/{id}")
	public ResponseEntity<?> removeCart(@PathVariable("cartId") String cartId,@PathVariable("id") int id) throws BadRequestException {

		log.debug("rest request to remove cart by id");

//		User user=userRepo.findById(userId).get();
		
		Cart cart=cartRepo.findById(cartId).get();
		
		for(int i=0;i<cart.getItems().size();i++)
			if(cart.getItems().get(i).getId()==id)
				cart.getItems().remove(i);

		Cart result = cartRepo.save(cart);
		return new ResponseEntity<>(new ResultStatus("Success", "Cart removed"), HttpStatus.OK);
	}

}
