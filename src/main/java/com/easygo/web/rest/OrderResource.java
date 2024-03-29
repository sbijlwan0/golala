package com.easygo.web.rest;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.easygo.domain.Cart;
import com.easygo.domain.Order;
import com.easygo.domain.Organisation;
import com.easygo.domain.Payment;
import com.easygo.domain.Product;
import com.easygo.domain.SequenceGenerator;
import com.easygo.domain.User;
import com.easygo.domain.Wallet;
import com.easygo.repository.AuthorityRepository;
import com.easygo.repository.CartRepository;
import com.easygo.repository.OrderRepository;
import com.easygo.repository.OrganisationRepository;
import com.easygo.repository.PaymentRepository;
import com.easygo.repository.ProductRepository;
import com.easygo.repository.SequenceGeneratorRepository;
import com.easygo.repository.UserRepository;
import com.easygo.repository.WalletRepository;
import com.easygo.security.AuthoritiesConstants;
import com.easygo.security.SecurityUtils;
import com.easygo.service.PushService;
import com.easygo.service.dto.ChecksumResponse;
import com.easygo.service.dto.Item;
import com.easygo.service.dto.ProductDTO;
import com.easygo.service.dto.ResultStatus;
import com.easygo.service.dto.SubProduct;
import com.easygo.service.util.RandomUtil;
import com.paytm.pg.merchant.CheckSumServiceHelper;

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

	@Autowired
	UserRepository userRepo;

	@Autowired
	CartRepository cartRepo;

	@Autowired
	AuthorityRepository authRepo;

	@Autowired
	MongoTemplate mongoTemplate;

	@Autowired
	OrganisationRepository orgRepo;
	
	@Autowired
	SequenceGeneratorRepository sequence;
	
	@Autowired
	PaymentRepository paymentRepo;
	
	@Value("${paytm.mkey}")
	private String MercahntKey;
	
	@Autowired
	WalletRepository walRepo;

	@PostMapping("/order")
	public ResponseEntity<?> placeOrder(@Valid @RequestBody Order order) throws BadRequestException {

		log.debug("rest request to place order.");

		if (null != order.getId() || order.isReturnOrder())
			throw new BadRequestException("Invalid Order.");

		Order or=validateItemList(order);

		List<Order> orders = new ArrayList<Order>();

		orders = vendorWise(or);

		Cart cart = cartRepo.findByUserId(order.getUserId()).get();
		List<ProductDTO> items = new ArrayList<>();
		cart.setItems(items);
		cartRepo.save(cart);
		
		orders.forEach(o->{
		if(!o.getStatus().equalsIgnoreCase("Cancelled")) {
		o.setPaymentStatus("paid");
		orderRepo.save(o);
		Wallet wal=new Wallet();
		try {
		wal=walRepo.findOneByVendorId(orgRepo.findById(o.getOrgId()).get().getVendorId()).get();
		}catch(Exception e){
			wal.setVendorId(orgRepo.findById(o.getOrgId()).get().getVendorId());
		}
		wal.setAmount(wal.getAmount()+o.getPrice());
		wal.setLastModifiedDate(Instant.now());
		walRepo.save(wal);}
		});

		for (Order o : orders) {
			try {
			push.sendOrderPlacedPush(userRepo.findById(o.getUserId()).get().getFcmTokens(), o);
			push.sendOrderPlacedPush(orgRepo.findById(o.getOrgId()).get().getVendor().getFcmTokens(), o);
			List<User> users = userRepo.findAllByAuthoritiesContainsAndLiveLocationNear(
					authRepo.findById(AuthoritiesConstants.DELIVERER).get(),
					new Point(orgRepo.findById(o.getOrgId()).get().getLocation().getX(),
							orgRepo.findById(o.getOrgId()).get().getLocation().getY()),
					new Distance(7, Metrics.KILOMETERS));

			for (User user : users)
				push.sendOrderPlacedPush(user.getFcmTokens(), o);
			}catch(Exception a) {
				a.printStackTrace();
			}
		}

		return new ResponseEntity<>(new ResultStatus("Success", "Order Placed", orders), HttpStatus.CREATED);
	}

	@PutMapping("/order")
	public ResponseEntity<?> updateOrder(@Valid @RequestBody Order order) throws BadRequestException {

		log.debug("rest request to update order.");

		if (null == order.getId() || order.isReturnOrder())
			throw new BadRequestException("Invalid Order.");

		if (order.getStatus().equalsIgnoreCase("Delivered"))
			throw new BadRequestException("order is delivered. can't be updated now.");

		reverseOrder(order);

		Order result = orderRepo.save(order);

		return new ResponseEntity<>(new ResultStatus("Success", "Order Updated", result), HttpStatus.OK);
	}

	@PutMapping("/updateOrderStatus/{otp}/{status}")
	public ResponseEntity<?> verifyOrderOTP(@PathVariable("otp") String otp, @PathVariable("status") String status,
			@RequestBody Order order) throws BadRequestException {

		try {
			User user=userRepo.findOneByLogin(SecurityUtils.getCurrentUserLogin().get()).get();
			
		if(!order.getDelivererId().equalsIgnoreCase(user.getId()))
			return new ResponseEntity<>(new ResultStatus("Error", "Wrong driver for this order."), HttpStatus.BAD_REQUEST);
			
			switch (status) {

			case "Picked":
				if (order.getVendorOtp().equalsIgnoreCase(otp)) {
					order.setStatus(status);
					order.getItems().forEach(pro->{
						pro.setStatus("Picked");
					});
//					push.sendOrderPickedPush(orgRepo.findById(order.getOrgId()).get().getVendor().getFcmTokens(), order);
					break;
				}
				return new ResponseEntity<>(new ResultStatus("Error", "Invalid OTP"), HttpStatus.BAD_REQUEST);

			case "Delivered":
				if (order.getCustomerOtp().equalsIgnoreCase(otp)) {
					order.setStatus(status);
					order.setDeliveryTime(Instant.now());
					order.getItems().forEach(pro->{
						pro.setStatus("Delivered");
					});
//					push.sendOrderPickedPush(userRepo.findById(order.getUserId()).get().getFcmTokens(), order);
					break;
				}
				return new ResponseEntity<>(new ResultStatus("Error", "Invalid OTP"), HttpStatus.BAD_REQUEST);

			default:
				return new ResponseEntity<>(new ResultStatus("Error", "Invalid Status"), HttpStatus.BAD_REQUEST);

			}

			Order result = orderRepo.save(order);

			return new ResponseEntity<>(new ResultStatus("Success", "Status Updated", result), HttpStatus.OK);
		}catch(Exception e){
			return new ResponseEntity<>(new ResultStatus("Error", "Please Login"), HttpStatus.BAD_REQUEST);
		}

	}

	@PutMapping("/CancelOrder/{orderId}")
	public ResponseEntity<?> cancelOrder(@PathVariable("orderId") String orderId, @RequestBody List<Integer> ids) {

		log.debug("rest request to cancel Order");

		Order order = orderRepo.findById(orderId).get();

		if (ids.size() == 0 || ids.isEmpty()) {
			if (!order.getStatus().equalsIgnoreCase("Cancelled"))
				order.setStatus("Cancelled");
			order.getItems().forEach(pro->ids.add(pro.getId()));

		}

		order = updateProduct(order, ids);
		
		int n=0;
		for(ProductDTO item:order.getItems())
			if(item.getStatus().equalsIgnoreCase("Cancelled"))
				n++;
		
		if(order.getItems().size()==n)
			order.setStatus("Cancelled");

		Order result = orderRepo.save(order);

		return new ResponseEntity<>(new ResultStatus("Success", "Order Cancelled", result), HttpStatus.OK);
	}
	
	
	@GetMapping("/getRootOrderId")
	public ResponseEntity<?> getRootOrderId(){
		log.debug("generating root order id");
		
		String id="";
		
		SequenceGenerator seq=new SequenceGenerator();
		if(sequence.findOneByType("order").isPresent()) {
			seq=sequence.findOneByType("order").get();
			id=String.valueOf((Long.valueOf(seq.getSequence())+1));
			seq.setSequence(id);
		}
		else {
			seq.setType("order");
			seq.setSequence(String.valueOf(new Date().getTime()));
			id=seq.getSequence();
		}
		sequence.save(seq);
		
		return new ResponseEntity<>(new ResultStatus("Success","root Order Id Generated",id),HttpStatus.OK);
	}


	@GetMapping("/order/{page}")
	public ResponseEntity<?> getAllOrder(@PathVariable("page") int page) {

		log.debug("rest request to get All order.");
		
		Sort sort = new Sort(Sort.Direction.DESC,"created_date");

		return new ResponseEntity<>(new ResultStatus("Success", "Order Fetched",
				orderRepo.findAllByReturnOrder(false, PageRequest.of(page, 10,sort))), HttpStatus.OK);
	}

	@GetMapping("/orderByUserId/{userId}/{page}")
	public ResponseEntity<?> getOrderByUser(@PathVariable("userId") String userId, @PathVariable("page") int page) {

		log.debug("rest request to get order by user.");
		
		Sort sort = new Sort(Sort.Direction.DESC,"created_date");

		return new ResponseEntity<>(new ResultStatus("Success", "Order Fetched",
				orderRepo.findByReturnOrderAndUserId(false, userId, PageRequest.of(page, 10,sort))), HttpStatus.OK);
	}

	@GetMapping("/orderById/{id}")
	public ResponseEntity<?> getOrderById(@PathVariable("id") String id) throws BadRequestException {

		log.debug("rest request to get order by id.");

		if (!orderRepo.findById(id).isPresent())
			throw new BadRequestException("order not found");

		return new ResponseEntity<>(new ResultStatus("Success", "Order Fetched", orderRepo.findById(id).get()),
				HttpStatus.OK);
	}
//
//	@GetMapping("/driverPendingOrders/{id}")
//	public ResponseEntity<?> getPendingOrderForDriver(@PathVariable("id") String id) throws BadRequestException {
//
//		log.debug("rest request to get order by id.");
//
//		if (!userRepo.findById(id).isPresent() && !userRepo.findById(id).get().getAuthorities()
//				.contains(authRepo.findById(AuthoritiesConstants.DELIVERER).get()))
//			throw new BadRequestException("driver not found");
//		User user=userRepo.findById(id).get();
//
//		return new ResponseEntity<>(new ResultStatus("Success", "Order Fetched", orderRepo.findByDriverAssignedAndLocationNear(false, new Point(user.getAddress()., filter.getLongitude()),
//				new Distance(filter.getDistance(), Metrics.KILOMETERS))),
//				HttpStatus.OK);
//	}


	@DeleteMapping("/removeOrderById/{id}")
	public ResponseEntity<?> removeOrderById(@PathVariable("id") String id) throws BadRequestException {

		log.debug("rest request to remove order by id.");

		if (!orderRepo.findById(id).isPresent())
			throw new BadRequestException("order not found");

		orderRepo.deleteById(id);

		return new ResponseEntity<>(new ResultStatus("Success", "Order Removed"), HttpStatus.OK);
	}

	@GetMapping("vendorOrdersByOrgId/{orgId}")
	public ResponseEntity<?> vendorOrdersByOrgId(@PathVariable("orgId") String orgId, @RequestParam("page") int page)
			throws BadRequestException {

		log.debug("rest request to cancel order by id");
		Sort sort = new Sort(Sort.Direction.DESC,"created_date");

		return new ResponseEntity<>(
				new ResultStatus("Success", "Order Fetched", orderRepo.findAllByOrgId(orgId, PageRequest.of(page, 10,sort))),
				HttpStatus.OK);
	}
	
	
	@GetMapping("vendorOrders/{id}")
	public ResponseEntity<?> vendorOrders(@PathVariable("id") String id, @RequestParam("page") int page)
			throws BadRequestException {
		
		Sort sort = new Sort(Sort.Direction.DESC,"created_date");
		
		List<Organisation>orgs=orgRepo.findAllByVendorId(id);
		List<String>ids=new ArrayList<>();
		
		for(Organisation org : orgs)
			ids.add(org.getId());
	

		log.debug("rest request to cancel order by id");

		return new ResponseEntity<>(
				new ResultStatus("Success", "Order Fetched", orderRepo.findAllByOrgIdIn(ids, PageRequest.of(page, 10,sort))),
				HttpStatus.OK);
	}
	
	
	@PostMapping("/getChecksum")
		public ResponseEntity<?> generateChecksum(@RequestBody TreeMap<String,String> paramMap ) throws BadRequestException {
		
		try {
			User user=userRepo.findOneByLogin(SecurityUtils.getCurrentUserLogin().get()).get();
			
			Payment pay=new Payment();
			
//		if(paymentRepo.findOneByRootOrderIdAndSuccessIsTrue(paramMap.get("ORDERID")).isPresent())
//			throw new BadRequestException("Payment For This Order Already Exist");
		
		for (Map.Entry<String, String> entry : paramMap.entrySet())
		{   
				if(entry.getKey().equalsIgnoreCase("ORDER_ID")) 
					pay.setRootOrderId(entry.getValue());		
		}
		
		pay.setUserId(user.getId());
		pay.setParams(paramMap);
//		pay.setRootOrderId(paramMap.get("ORDERID").ge);
		paymentRepo.save(pay);
			ChecksumResponse check=new ChecksumResponse();
		try{
			check.setChecksum(CheckSumServiceHelper.getCheckSumServiceHelper().genrateCheckSum(MercahntKey, paramMap));
			
			
			System.out.println("Paytm Payload: "+ check.getChecksum());
			
			}catch(Exception e) {
				// TODO Auto-generated catch block
				check.setChecksum(e.toString());
				e.printStackTrace();
			}
		
		return new ResponseEntity<>(new ResultStatus("Success","checksum generated",check),HttpStatus.OK);
				}catch(Exception e){
			return new ResponseEntity<>(new ResultStatus("Error","Please Login"),HttpStatus.BAD_REQUEST);
		}
		}
	
	
	@PostMapping("/verifyChecksum")
	public ResponseEntity<?> verifyChecksum(@RequestBody TreeMap<String,String> mapData) {
		
		try {
			User user=userRepo.findOneByLogin(SecurityUtils.getCurrentUserLogin().get()).get();
			
			List<Order> orders=new ArrayList<Order>();	
		String paytmChecksum = "";
		String rootId="";

		
		TreeMap<String, String> paytmParams = new  TreeMap<String,String>();
		
		for (Map.Entry<String, String> entry : mapData.entrySet())
		{   
		    if(entry.getKey().equalsIgnoreCase("CHECKSUMHASH")){
				paytmChecksum = entry.getValue();
			}else{
				paytmParams.put(entry.getKey(), entry.getValue());
				if(entry.getKey().equalsIgnoreCase("ORDERID")) 
					rootId=entry.getValue();
				
			}
		}
		
		Sort sort = new Sort(Sort.Direction.DESC,"created_date");
		
		List<Payment> payments=paymentRepo.findAllByRootOrderId(rootId,sort);
		
		Payment pay=payments.get(0);
		pay.setParams(paytmParams);
		
		boolean isValideChecksum = false;
		System.out.println(paytmParams);
		try{
			
			isValideChecksum = CheckSumServiceHelper.getCheckSumServiceHelper().verifycheckSum(MercahntKey, paytmParams,paytmChecksum);
			
			System.out.println(isValideChecksum);
			pay.setSuccess(isValideChecksum);
			paymentRepo.save(pay);
//			orders=orderRepo.findAllByRootOrderId(pay.getRootOrderId());
			if(isValideChecksum) {
				
				return new ResponseEntity<>(new ResultStatus("Success", "Payment Successfull",pay), HttpStatus.OK);
				
//				orders.forEach(o->{
//					if(!o.getStatus().equalsIgnoreCase("Cancelled")) {
//					o.setPaymentStatus("paid");
//					orderRepo.save(o);
//					Wallet wal=new Wallet();
//					try {
//					wal=walRepo.findOneByVendorId(orgRepo.findById(o.getOrgId()).get().getVendorId()).get();
//					}catch(Exception e){
//						wal.setVendorId(orgRepo.findById(o.getOrgId()).get().getVendorId());
//					}
//					wal.setAmount(wal.getAmount()+o.getPrice());
//					wal.setLastModifiedDate(Instant.now());
//					walRepo.save(wal);}
//					});
			}
			else {
				return new ResponseEntity<>(new ResultStatus("Error", "Payment Failed",null), HttpStatus.BAD_REQUEST);
			}
				
		}catch(Exception e){
			e.printStackTrace();
		}
		return new ResponseEntity<>(new ResultStatus("Success", "Payment Successfull",orders), HttpStatus.OK);
		}catch(Exception e){
			return new ResponseEntity<>(new ResultStatus("Error", "Please Login"), HttpStatus.BAD_REQUEST);
		}
	}
	

	public Order validateItemList(Order order) throws BadRequestException {
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
							else {
								product.setStatus("Cancelled - insufficient product quatity");
							}

					}
				}
				if (n == 2)
					proRepo.save(pro);
			}
		}
		return order;
//		if (!order.getStatus().equalsIgnoreCase("Cancelled"))
//			if(null!=order.getCouponCode())
//			price = price - order.getDiscount();
//		if (price != order.getPrice())
//			order.setPrice(price);
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
						if (n == 2 && !order.getStatus().equalsIgnoreCase("Cancelled")
								&& !order.getItems().get(i).getStatus().equalsIgnoreCase("Cancelled")) {
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
//		if (!order.getStatus().equalsIgnoreCase("Cancelled"))
//			if(null!=order.getCouponCode())
//			price = price - order.getDiscount();
//		if (price != order.getPrice())
//			order.setPrice(price);
	}

	public Order updateProduct(Order order, List<Integer> ids) {

		for (ProductDTO product : order.getItems()) {

			if (ids.contains(product.getId())) {
				Product pro=new Product();
				try {
				pro = proRepo.findById(product.getProductId()).get();
				}catch(Exception e) {
					System.out.println("product "+product.getName()+" "+"not found in database.");
				}
				for (SubProduct spro : pro.getSubProduct())
					if (spro.getId() == product.getSubProductId())
						spro.setQuantity(spro.getQuantity() + product.getQuantity());
//				order.setPrice(order.getPrice() - (product.getDiscountPrice() * product.getQuantity()));
				product.setStatus("Cancelled");
				proRepo.save(pro);
			}

		}
		return order;
	}

	public List<Order> vendorWise(Order order) {
		List<String> orgIds = new ArrayList<>();
		List<Order> orders = new ArrayList<Order>();
		List<Order> forder = new ArrayList<Order>();
		Long on;
		SequenceGenerator seq=new SequenceGenerator();
		if(sequence.findOneByType("orderNo").isPresent()) {
			seq=sequence.findOneByType("orderNo").get();
			on=Long.valueOf(seq.getSequence())+1;
			seq.setSequence(String.valueOf(on));
		}
		else {
			seq.setType("orderNo");
			on=new Date().getTime();
			seq.setSequence(String.valueOf(on));
		}
		

		for (ProductDTO product : order.getItems()) {

			Order o = new Order();
			List<ProductDTO> items = new ArrayList<ProductDTO>();
			items.add(product);
			o.setBillingAddress(order.getBillingAddress());
			o.setDeliveryAddress(order.getDeliveryAddress());
			o.setCouponCode(order.getCouponCode());
			o.setDiscount(order.getDiscount());
			o.setRootOrderId(order.getRootOrderId());
			o.setDiscountedAmount(order.getDiscountedAmount() / order.getItems().size());
			o.setUserId(order.getUserId());
			o.setPrice(product.getDiscountPrice());
			o.setOrgId(proRepo.findById(product.getProductId()).get().getOrganisationId());
			o.setItems(items);

			o.setCustomerOtp(RandomUtil.generateOTP());

			o.setVendorOtp(RandomUtil.generateOTP());
			
			o.setOrderNo(String.valueOf(on+=1));
			seq.setSequence(o.getOrderNo());

			


			orders.add(o);

		}

		for (Order or : orders) {

			if (!orgIds.contains(or.getOrgId()))
				orgIds.add(or.getOrgId());

		}

		for (String id : orgIds) {
			Order o = new Order();
			o.setLocation(orgRepo.findById(id).get().getLocation());
			List<ProductDTO> product = new ArrayList<ProductDTO>();
			for (Order or : orders) {

				if (id.equalsIgnoreCase(or.getOrgId())) {
					product.add(or.getItems().get(0));
					o.setBillingAddress(or.getBillingAddress());
					o.setDeliveryAddress(or.getDeliveryAddress());
					o.setCouponCode(or.getCouponCode());
					o.setDiscount(or.getDiscount());
					o.setDiscountedAmount((o.getDiscountedAmount()) + or.getDiscountedAmount());
					o.setUserId(or.getUserId());
					o.setPrice(o.getPrice() + or.getPrice());
					o.setOrgId(id);
					o.setItems(product);
					o.setRootOrderId(or.getRootOrderId());
					o.setCustomerOtp(or.getCustomerOtp());

					o.setVendorOtp(or.getVendorOtp());
					try {
					o.setOrderNo(or.getOrderNo());
					}catch(Exception a) {
						o.setOrderNo(String.valueOf(on+=1));
						seq.setSequence(o.getOrderNo());
					}

				}
			}
			forder.add(orderRepo.save(o));
		}
		sequence.save(seq);
		return forder;
	}

}
