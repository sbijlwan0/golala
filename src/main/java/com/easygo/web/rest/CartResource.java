package com.easygo.web.rest;

import java.util.ArrayList;
import java.util.HashMap;
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
import com.easygo.domain.Product;
import com.easygo.domain.User;
import com.easygo.repository.CartRepository;
import com.easygo.repository.ProductRepository;
import com.easygo.repository.UserRepository;
import com.easygo.security.SecurityUtils;
import com.easygo.service.dto.ProductDTO;
import com.easygo.service.dto.ResultStatus;
import com.easygo.service.dto.SubProduct;

import io.undertow.util.BadRequestException;

@RestController
@RequestMapping("/api")
public class CartResource {

	private final Logger log = LoggerFactory.getLogger(CartResource.class);

	@Autowired
	UserRepository userRepo;
	
	@Autowired
	CartRepository cartRepo;
	
	@Autowired
	ProductRepository proRepo;

	@PostMapping("/cart")
	public ResponseEntity<?> createCart(@Valid @RequestBody ProductDTO product) throws BadRequestException {

		log.debug("rest request to add cart");

		if(!SecurityUtils.getCurrentUserLogin().isPresent() || SecurityUtils.getCurrentUserLogin().get().equalsIgnoreCase(""))
			return new ResponseEntity<>(new ResultStatus("Error","Login First"),HttpStatus.BAD_REQUEST);
		
		
		try {
		
		User user=userRepo.findOneByLogin(SecurityUtils.getCurrentUserLogin().get()).get();
		
		Cart cart=new Cart();
		boolean is=false;
		
		if(cartRepo.findByUserId(user.getId()).isPresent()) {
			cart=cartRepo.findByUserId(user.getId()).get();
			if(null==cart.getItems()) {
			List<ProductDTO> pros=new ArrayList<ProductDTO>();
			pros.add(product);
			cart.setItems(pros);}
			else {
//				Product prod=proRepo.findById(product.getProductId()).get();
				for(ProductDTO pro:cart.getItems()) {
					if(pro.getProductId().equalsIgnoreCase(product.getProductId()))
//						for(SubProduct item : prod.getSubProduct())
							if(product.getSubProductId()==pro.getSubProductId()) {
								pro.setQuantity(pro.getQuantity()+product.getQuantity());
								is=true;
							}
				}
				if(is==false)
					cart.getItems().add(product);
				
			}
				
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
	
	
	@GetMapping("/getCartProducts")
	public ResponseEntity<?> getCartProducts(){

		log.debug("rest request to get cart Products");
		
		try {
		User user=userRepo.findOneByLogin(SecurityUtils.getCurrentUserLogin().get()).get();
		
		List<Product>products=new ArrayList<Product>();
		
		Cart cart=cartRepo.findByUserId(user.getId()).get();
		for(ProductDTO pro:cart.getItems()) {
			try {
			Product prod=proRepo.findById(pro.getProductId()).get();
			products.add(prod);
			}catch(Exception a) {
				products.add(null);
			}
		}
		return new ResponseEntity<>(new ResultStatus("Success", "Products Fetched", products),HttpStatus.OK);
			
		}catch(Exception a) {
			return new ResponseEntity<>(new ResultStatus("Error", "Login First"),HttpStatus.BAD_REQUEST);
		}

	}
	
	
	@GetMapping("/isInCart/{userId}/{productId}")
	public ResponseEntity<?> isProductInCart(@PathVariable("userId")String userId, @PathVariable("productId") String productId){
		
		log.debug("is the product in cart");
		HashMap<Integer, Boolean> result=new HashMap<>();
		
		Cart cart=cartRepo.findByUserId(userId).get();
		
		Product prod=proRepo.findById(productId).get();
		
		for(ProductDTO pro:cart.getItems()) {
			if(pro.getProductId().equalsIgnoreCase(productId))
				for(SubProduct item : prod.getSubProduct())
					if(item.getId()==pro.getSubProductId()) {
						result.put(item.getId(), true);
					}
					else {
						result.put(item.getId(), false);
					}
						
		}
		
		return new ResponseEntity<>(new ResultStatus("Success", "Order Fetched",result), HttpStatus.OK);
		
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
