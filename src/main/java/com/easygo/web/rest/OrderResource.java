package com.easygo.web.rest;

import java.time.Instant;

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

import com.easygo.domain.Order;
import com.easygo.domain.Product;
import com.easygo.repository.OrderRepository;
import com.easygo.repository.ProductRepository;
import com.easygo.service.PushService;
import com.easygo.service.dto.ProductDTO;
import com.easygo.service.dto.ResultStatus;
import com.easygo.service.dto.SubProduct;
import com.easygo.service.util.RandomUtil;

import io.undertow.util.BadRequestException;

@RestController
@RequestMapping("/api")
public class OrderResource {

	private final Logger log = LoggerFactory.getLogger(OrderResource.class);

	@Autowired
	OrderRepository orderRepo;

	@Autowired
	ProductRepository proRepo;
	
	@Autowired
	PushService push;

	@PostMapping("/order")
	public ResponseEntity<?> placeOrder(@Valid @RequestBody Order order) throws BadRequestException {

		log.debug("rest request to place order.");

		if (null != order.getId() || order.isReturnOrder())
			throw new BadRequestException("Invalid Order.");

		validateItemList(order);
		
		order.setCustomerOtp(RandomUtil.generateOTP());
		
		order.setVendorOtp(RandomUtil.generateOTP());

		Order result = orderRepo.save(order);

		return new ResponseEntity<>(new ResultStatus("Success", "Order Placed", result), HttpStatus.CREATED);
	}

	@PutMapping("/order")
	public ResponseEntity<?> updateOrder(@Valid @RequestBody Order order) throws BadRequestException {

		log.debug("rest request to update order.");

		if (null == order.getId() || order.isReturnOrder())
			throw new BadRequestException("Invalid Order.");

		if(order.getStatus().equalsIgnoreCase("Delivered"))
			throw new BadRequestException("order is delivered. can't be updated now.");
		
		reverseOrder(order);

		Order result = orderRepo.save(order);

		return new ResponseEntity<>(new ResultStatus("Success", "Order Updated", result), HttpStatus.OK);
	}
	
	@PutMapping("updateStatus/{otp}/{status}")
	public ResponseEntity<?> verifyOTP(@PathVariable("otp") String otp, @PathVariable("status") String status, @RequestBody Order order) throws BadRequestException{
		
		switch(status){
			
		case "Picked": if(order.getVendorOtp().equalsIgnoreCase(otp)) {
			order.setStatus(status);
			break;
			}
		throw new BadRequestException("Invalid OTP");
		
		case "Delivered": if(order.getCustomerOtp().equalsIgnoreCase(otp)) {
			order.setStatus(status);
			order.setDeliveryTime(Instant.now());
			break;
		}
		throw new BadRequestException("Invalid OTP");
		
		default: throw new BadRequestException("Invalid Status");
		
		}
		
		Order result = orderRepo.save(order);
		
		return new ResponseEntity<>(new ResultStatus("Success","Status Updated",result),HttpStatus.OK);
	}

	@GetMapping("/order/{page}")
	public ResponseEntity<?> getAllOrder(@PathVariable("page") int page) {

		log.debug("rest request to get All order.");

		return new ResponseEntity<>(new ResultStatus("Success", "Order Fetched",
				orderRepo.findAllByReturnOrder(false, PageRequest.of(page, 10))), HttpStatus.OK);
	}

	@GetMapping("/orderByUserId/{userId}/{page}")
	public ResponseEntity<?> getOrderByUser(@PathVariable("userId") String userId, @PathVariable("page") int page) {

		log.debug("rest request to get order by user.");

		return new ResponseEntity<>(new ResultStatus("Success", "Order Fetched",
				orderRepo.findByReturnOrderAndUserId(false, userId, PageRequest.of(page, 10))), HttpStatus.OK);
	}

	@GetMapping("/orderById/{id}")
	public ResponseEntity<?> getOrderById(@PathVariable("id") String id) throws BadRequestException {

		log.debug("rest request to get order by id.");

		if (!orderRepo.findById(id).isPresent())
			throw new BadRequestException("order not found");

		return new ResponseEntity<>(new ResultStatus("Success", "Order Fetched", orderRepo.findById(id).get()),
				HttpStatus.OK);
	}

	@DeleteMapping("/removeOrderById/{id}")
	public ResponseEntity<?> removeOrderById(@PathVariable("id") String id) throws BadRequestException {

		log.debug("rest request to remove order by id.");

		if (!orderRepo.findById(id).isPresent())
			throw new BadRequestException("order not found");

		orderRepo.deleteById(id);

		return new ResponseEntity<>(new ResultStatus("Success", "Order Removed"), HttpStatus.OK);
	}

	public void validateItemList(Order order) throws BadRequestException {
		double price = 0;
		for (int n = 1; n < 3; n++) {
			for (ProductDTO product : order.getItems()) {

				if (!proRepo.findById(product.getProductId()).isPresent())
					throw new BadRequestException("Invalid product in list with name " + product.getName());

				Product pro = proRepo.findById(product.getProductId()).get();

				for (SubProduct subPro : pro.getSubProduct()) {

					if (subPro.getId() == product.getSubProductId()) {
						if (n == 1)
							if (subPro.getQuantity() < product.getQuantity())
								throw new BadRequestException("Invalid quantity for sub Product id " + subPro.getId()
										+ " of name " + product.getName());
						if (n == 2)
							if (subPro.getQuantity() >= product.getQuantity()) {
								subPro.setQuantity(subPro.getQuantity() - product.getQuantity());
								price = price + (subPro.getDiscountPrice() * product.getQuantity());
							}

					}
				}
				if (n == 2)
					proRepo.save(pro);
			}
		}
		if (price != order.getPrice())
			order.setPrice(price);
	}

	public void reverseOrder(Order order) throws BadRequestException {
		double price = 0;
		Order oldOrder = orderRepo.findById(order.getId()).get();
		for (int n = 1; n < 3; n++) {
			for (int i = 0; i < order.getItems().size(); i++) {

				if (!proRepo.findById(order.getItems().get(i).getProductId()).isPresent())
					throw new BadRequestException(
							"Invalid product in list with name " + order.getItems().get(i).getName());

				Product pro = proRepo.findById(order.getItems().get(i).getProductId()).get();
				for (int j = 0; j < pro.getSubProduct().size(); j++) {

					if (pro.getSubProduct().get(j).getId() == order.getItems().get(i).getSubProductId()) {
						if (n == 1) {
							pro.getSubProduct().get(j).setQuantity(pro.getSubProduct().get(j).getQuantity()
									+ oldOrder.getItems().get(i).getQuantity());
							if (pro.getSubProduct().get(j).getQuantity() < order.getItems().get(i).getQuantity())
								throw new BadRequestException(
										"Invalid quantity for sub Product id " + pro.getSubProduct().get(j).getId()
												+ " of name " + order.getItems().get(i).getName());
						}
						if (n == 2) {
							double q = 0;
							if (oldOrder.getItems().get(i).getQuantity() > order.getItems().get(i).getQuantity()) {
								q = oldOrder.getItems().get(i).getQuantity() - order.getItems().get(i).getQuantity();
								pro.getSubProduct().get(j).setQuantity(pro.getSubProduct().get(j).getQuantity() + q);
								price = price + (pro.getSubProduct().get(j).getDiscountPrice()
										* order.getItems().get(i).getQuantity());
							} else if (oldOrder.getItems().get(i).getQuantity() < order.getItems().get(i)
									.getQuantity()) {
								q = order.getItems().get(i).getQuantity() - oldOrder.getItems().get(i).getQuantity();
								pro.getSubProduct().get(j).setQuantity(pro.getSubProduct().get(j).getQuantity() - q);
								price = price + (pro.getSubProduct().get(j).getDiscountPrice()
										* order.getItems().get(i).getQuantity());
							}
						}

					}
				}
				if (n == 2)
					proRepo.save(pro);
			}
		}
		if (price != order.getPrice())
			order.setPrice(price);
	}
}
