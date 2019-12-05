package com.easygo.web.rest;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.easygo.domain.Order;
import com.easygo.domain.User;
import com.easygo.repository.AuthorityRepository;
import com.easygo.repository.OrderRepository;
import com.easygo.repository.UserRepository;
import com.easygo.security.AuthoritiesConstants;
import com.easygo.security.SecurityUtils;
import com.easygo.service.dto.ResultStatus;

import io.undertow.util.BadRequestException;

@RestController
@RequestMapping("/api")
public class DriverResource {
	
	private final Logger log = LoggerFactory.getLogger(DriverResource.class);
	
	@Autowired
	UserRepository userRepo;
	
	@Autowired
	OrderRepository orderRepo;
	
	@Autowired
	AuthorityRepository authRepo;
	
	@GetMapping("/driverOrders")
	public ResponseEntity<?> getOrderForDriver(@RequestParam("page") int page)
			throws BadRequestException {

		log.debug("rest request to get order by id.");

		try {
			User user=userRepo.findOneByLogin(SecurityUtils.getCurrentUserLogin().get()).get();
			
			if(!user.getAuthorities().contains(authRepo.findById(AuthoritiesConstants.DELIVERER).get()))
				return new ResponseEntity<>(new ResultStatus("Error", "You are not a golala driver"), HttpStatus.BAD_REQUEST);

		return new ResponseEntity<>(
				new ResultStatus("Success", "Order Fetched", orderRepo.findByDelivererId(user.getId(), PageRequest.of(page, 10))),
				HttpStatus.OK);
		}catch(Exception e){
			return new ResponseEntity<>(new ResultStatus("Error", "Please Login"), HttpStatus.BAD_REQUEST);
		}
	}
	
	@GetMapping("/acceptOrder/{orderId}")
	public ResponseEntity<?> acceptOrder(@PathVariable("orderId") String orderId)
			throws BadRequestException {

		log.debug("rest request to accept order by id.");
		
		try {
			User user=userRepo.findOneByLogin(SecurityUtils.getCurrentUserLogin().get()).get();
			
			if(!user.getAuthorities().contains(authRepo.findById(AuthoritiesConstants.DELIVERER).get()))
				return new ResponseEntity<>(new ResultStatus("Error", "You are not a golala driver"), HttpStatus.BAD_REQUEST);
			
			Order order=orderRepo.findById(orderId).get();
			
			if(order.isDriverAssigned())
				return new ResponseEntity<>(new ResultStatus("Error", "Order is accepted by another driver already."), HttpStatus.BAD_REQUEST);
			
			order.setDelivererId(user.getId());
			
			order.setDriverAssigned(true);
			
			Order result = orderRepo.save(order);
			
			return new ResponseEntity<>(new ResultStatus("Success", "Driver Accepted",result), HttpStatus.OK);
			
		}catch(Exception e){
			return new ResponseEntity<>(new ResultStatus("Error", "Please Login"), HttpStatus.BAD_REQUEST);
		}
	}
	
	@GetMapping("/availableOrders")
	public ResponseEntity<?> viewAvailableOrders()
			throws BadRequestException {

		log.debug("rest request to view available orders");
		
		try {
			User user=userRepo.findOneByLogin(SecurityUtils.getCurrentUserLogin().get()).get();
			
			if(!user.getAuthorities().contains(authRepo.findById(AuthoritiesConstants.DELIVERER).get()))
				return new ResponseEntity<>(new ResultStatus("Error", "You are not a golala driver"), HttpStatus.BAD_REQUEST);
			
			
			List<Order>orders=orderRepo.findByDriverAssignedAndLocationNear(false, new Point(user.getLiveLocation().getX(),
					user.getLiveLocation().getY()),
			new Distance(7, Metrics.KILOMETERS));
			
			return new ResponseEntity<>(new ResultStatus("Success", "orders Fetched",orders), HttpStatus.OK);
			
		}catch(Exception e){
			return new ResponseEntity<>(new ResultStatus("Error", "Please Login"), HttpStatus.BAD_REQUEST);
		}
	}
	
	@GetMapping("/cancelOrder/{orderId}")
	public ResponseEntity<?> cancelOrder(@PathVariable("orderId") String orderId)
			throws BadRequestException {

		log.debug("rest request to accept order by id.");
		
		try {
			User user=userRepo.findOneByLogin(SecurityUtils.getCurrentUserLogin().get()).get();
			
			if(!user.getAuthorities().contains(authRepo.findById(AuthoritiesConstants.DELIVERER).get()))
				return new ResponseEntity<>(new ResultStatus("Error", "You are not a golala driver"), HttpStatus.BAD_REQUEST);
			
			Order order=orderRepo.findById(orderId).get();
			
			if(!order.getStatus().equalsIgnoreCase("processing"))
				return new ResponseEntity<>(new ResultStatus("Error", "You cannot cancel this order now. as you have picked the package."), HttpStatus.BAD_REQUEST);
			
			if(!order.getDelivererId().equalsIgnoreCase(user.getId()))
				return new ResponseEntity<>(new ResultStatus("Error", "You are not eligible to perform action in this order."), HttpStatus.BAD_REQUEST);
			
			order.setDelivererId(user.getId());
			
			order.setDriverAssigned(true);
			
			Order result = orderRepo.save(order);
			
			return new ResponseEntity<>(new ResultStatus("Success", "Driver Accepted",result), HttpStatus.OK);
			
		}catch(Exception e){
			return new ResponseEntity<>(new ResultStatus("Error", "Please Login"), HttpStatus.BAD_REQUEST);
		}
	}
	

}
