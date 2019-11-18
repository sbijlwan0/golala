package com.easygo.web.rest;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
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

import com.easygo.domain.Organisation;
import com.easygo.domain.Product;
import com.easygo.repository.OrganisationRepository;
import com.easygo.repository.ProductRepository;
import com.easygo.service.dto.AttributeDTO;
import com.easygo.service.dto.Filters;
import com.easygo.service.dto.ResultStatus;
import com.easygo.service.dto.SubProduct;

import io.undertow.util.BadRequestException;

@RestController
@RequestMapping("/api")
public class ProductResource {

	private final Logger log = LoggerFactory.getLogger(ProductResource.class);

	@Autowired
	ProductRepository proRepo;

	@Autowired
	OrganisationRepository orgRepo;

	@PostMapping("/product")
	public ResponseEntity<?> addProduct(@Valid @RequestBody Product product) throws BadRequestException {

		log.debug("rest request to add product");

		if (null != product.getId())
			throw new BadRequestException("Id must be null");

		if (!orgRepo.findById(product.getOrganisationId()).isPresent())
			throw new BadRequestException("invalid organisation id");

		product.setOrganisation(orgRepo.findById(product.getOrganisationId()).get());

		if (!product.getOrganisation().isActivated())
			product.setActive(false);

		if (!product.getSubProduct().get(0).getImages().isEmpty() || null != product.getSubProduct().get(0).getImages())
			product.setImage(product.getSubProduct().get(0).getImages().get(0));

		Product result = proRepo.save(product);

		return new ResponseEntity<>(new ResultStatus("Success", "Product Created", result), HttpStatus.CREATED);

	}

	@PutMapping("/product")
	public ResponseEntity<?> updateProduct(@Valid @RequestBody Product product) throws BadRequestException {

		log.debug("rest request to update product");

		if (null == product.getId())
			throw new BadRequestException("Id must not be null");

		if (!orgRepo.findById(product.getOrganisationId()).isPresent())
			throw new BadRequestException("invalid organisation id");

		product.setOrganisation(orgRepo.findById(product.getOrganisationId()).get());

		if (!product.getOrganisation().isActivated())
			product.setActive(false);

		if (!product.getSubProduct().get(0).getImages().isEmpty() || null != product.getSubProduct().get(0).getImages())
			product.setImage(product.getSubProduct().get(0).getImages().get(0));

		Product result = proRepo.save(product);

		return new ResponseEntity<>(new ResultStatus("Success", "Product Updated", result), HttpStatus.OK);

	}

	@GetMapping("/product")
	public ResponseEntity<?> getAllProduct() {

		log.debug("rest request to get all product.");

		return new ResponseEntity<>(new ResultStatus("Success", "Product Fetched", proRepo.findAll()), HttpStatus.OK);
	}
	
	@GetMapping("/product/{id}")
	public ResponseEntity<?> getProductById(@PathVariable("id")String id) throws BadRequestException {

		log.debug("rest request to get product by id.");
		
		if(!proRepo.findById(id).isPresent())
			throw new BadRequestException("invalid product id");

		return new ResponseEntity<>(new ResultStatus("Success", "Product Fetched", proRepo.findById(id).get()), HttpStatus.OK);
	}

	@GetMapping("/activateproduct/{id}")
	public ResponseEntity<?> activateProduct(@PathVariable("id") String id) throws BadRequestException {

		log.debug("rest request to activate product.");

		if (!proRepo.findById(id).isPresent())
			throw new BadRequestException("invalid product id");

		Product pro = proRepo.findById(id).get();

		if (pro.isActive())
			pro.setActive(false);
		else
			pro.setActive(true);

		proRepo.save(pro);

		return new ResponseEntity<>(new ResultStatus("Success", "Product Updated"), HttpStatus.OK);
	}

	@GetMapping("/allProductByOrganisationId/{id}/{page}")
	public ResponseEntity<?> getAllProductByOrganisationId(@PathVariable("id") String id,
			@PathVariable("page") int page) throws BadRequestException {

		log.debug("rest request to get all product by organisation id.");

		if (!orgRepo.findById(id).isPresent())
			throw new BadRequestException("invalid organisation id");

		return new ResponseEntity<>(new ResultStatus("Success", "Product Fetched",
				proRepo.findAllByOrganisationId(id, PageRequest.of(page, 10))), HttpStatus.OK);
	}

	@GetMapping("/productByCategory/{category}/{page}")
	public ResponseEntity<?> getAllproductByCategory(@PathVariable("category") String category,
			@PathVariable("page") int page) throws BadRequestException {

		log.debug("rest request to get all product by organisation id.");

		return new ResponseEntity<>(new ResultStatus("Success", "Product Fetched",
				proRepo.findAllByActiveIsTrueAndCategory(category, PageRequest.of(page, 10))), HttpStatus.OK);
	}

	@GetMapping("/productByOrganisationId/{id}/{page}")
	public ResponseEntity<?> getAllproductByOrganisationId(@PathVariable("id") String id,
			@PathVariable("page") int page) throws BadRequestException {

		log.debug("rest request to get all product by organisation id.");

		if (!orgRepo.findById(id).isPresent())
			throw new BadRequestException("invalid organisation id");

		return new ResponseEntity<>(new ResultStatus("Success", "Product Fetched",
				proRepo.findAllByActiveIsTrueAndOrganisationId(id, PageRequest.of(page, 10))), HttpStatus.OK);
	}

	@PutMapping("/productByLatLong")
	public ResponseEntity<?> getAllproductByLatLong(@RequestBody Filters filter) throws BadRequestException {

		log.debug("rest request to get all product by lat long");
		
		Sort sort = new Sort(Sort.Direction.DESC, filter.getSortCol());
		if (filter.getSortDir().equalsIgnoreCase("asc"))
			sort = new Sort(Sort.Direction.ASC, filter.getSortCol());

		List<Organisation> orgs = orgRepo.findAllByActivatedAndOpenIsTrueAndLocationNear(true,
				new Point(filter.getLatitude(), filter.getLongitude()),
				new Distance(filter.getDistance(), Metrics.KILOMETERS));

		List<String> ids = new ArrayList<>();

		for (Organisation org : orgs) {
			ids.add(org.getId());
		}

		if (null == filter.getCategory())
			return new ResponseEntity<>(new ResultStatus("Success", "Product Fetched",
					proRepo.findAllByActiveIsTrueAndOrganisationIdIn(ids, PageRequest.of(filter.getPage(), 10,sort))),
					HttpStatus.OK);

		return new ResponseEntity<>(new ResultStatus("Success", "Product Fetched",
				proRepo.findAllByActiveIsTrueAndCategoryAndOrganisationIdIn(filter.getCategory(), ids,
						PageRequest.of(filter.getPage(), 10,sort))),
				HttpStatus.OK);
	}

	@DeleteMapping("/removeProduct/{id}")
	public ResponseEntity<?> removeProduct(@PathVariable("id") String id) {

		log.debug("rest request to remove Product");

		proRepo.deleteById(id);

		return new ResponseEntity<>(new ResultStatus("Success", "Product removed"), HttpStatus.OK);
	}

	@GetMapping("/getProduct")
	public List<AttributeDTO> getlist() {

		List<Product> pro = proRepo.findAll();
		Product prod = pro.get(0);
		int s = 0;
		SubProduct sub = new SubProduct();
		for (SubProduct product : prod.getSubProduct()) {
			if (product.getAttributes().size() > s) {
				s = product.getAttributes().size();
				sub = product;
			}
		}
		List<AttributeDTO> finalList = new ArrayList<AttributeDTO>();
		for (AttributeDTO attr : sub.getAttributes()) {
			AttributeDTO att = new AttributeDTO();
			att.setAttributeLabel(attr.getAttributeLabel());
			att.setViewType(attr.getViewType());
			List<String> value = new ArrayList<>();
			for (int i = 0; i < prod.getSubProduct().size(); i++) {
				for (int j = 0; j < prod.getSubProduct().get(i).getAttributes().size(); j++) {
					if (prod.getSubProduct().get(i).getAttributes().get(j).getAttributeLabel()
							.equalsIgnoreCase(att.getAttributeLabel()))
						value.add(prod.getSubProduct().get(i).getAttributes().get(j).getValue().toString());
				}
			}
			att.setValue(value);
			finalList.add(att);
		}
		return finalList;
	}
}
