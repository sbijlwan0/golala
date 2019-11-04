package com.easygo.web.rest;

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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.easygo.domain.Organisation;
import com.easygo.repository.AuthorityRepository;
import com.easygo.repository.OrganisationRepository;
import com.easygo.repository.UserRepository;
import com.easygo.security.AuthoritiesConstants;
import com.easygo.service.MailService;
import com.easygo.service.dto.Filters;
import com.easygo.service.dto.ResultStatus;

import io.undertow.util.BadRequestException;

@RestController
@RequestMapping("/api")
public class OrganisationResource {

	private final Logger log = LoggerFactory.getLogger(OrganisationResource.class);

	@Autowired
	OrganisationRepository orgRepo;

	@Autowired
	UserRepository userRepo;

	@Autowired
	MailService mailService;

	@Autowired
	AuthorityRepository authRepo;

	@PostMapping("/organisation")
	public ResponseEntity<?> addOrganisation(@Valid @RequestBody Organisation org) throws BadRequestException {

		log.debug("Rest Request To add Organisation.");

		if (null != org.getId())
			throw new BadRequestException("Id Should Be null");

		org.setActivated(false);

		if (userRepo.findById(org.getVendorId()).isPresent() && userRepo.findById(org.getVendorId()).get()
				.getAuthorities().contains(authRepo.findById(AuthoritiesConstants.VENDOR).get()))
			org.setVendor(userRepo.findById(org.getVendorId()).get());
		else
			throw new BadRequestException("You are not a vendor. Please register as vendor first.");

		Organisation result = orgRepo.save(org);

		return new ResponseEntity<>(new ResultStatus("Success", "Organisation Added", result), HttpStatus.CREATED);

	}

	@PutMapping("/organisation")
	public ResponseEntity<?> updateOrganisation(@Valid @RequestBody Organisation org) throws BadRequestException {

		log.debug("Rest Request To update Organisation.");

		if (null == org.getId())
			throw new BadRequestException("Id must not Be null");

		Organisation result = orgRepo.save(org);

		return new ResponseEntity<>(new ResultStatus("Success", "Organisation Added", result), HttpStatus.OK);

	}

	@GetMapping("/activateOrganisation/{id}")
	public ResponseEntity<?> activateOrganisation(@PathVariable("id") String id) throws BadRequestException {

		log.debug("Rest Request To activate Organisation.");

		if (null == id)
			throw new BadRequestException("Id must not Be null");

		if (!orgRepo.findById(id).isPresent()) {

			throw new BadRequestException("Organisation Not Found.");

		}
		Organisation org = orgRepo.findById(id).get();
		if(org.isActivated())
			org.setActivated(false);
		else
			org.setActivated(true);

		Organisation result = orgRepo.save(org);

		mailService.sendOrganisationActivationMail(result, userRepo.findById(result.getVendorId()).get());

		return new ResponseEntity<>(new ResultStatus("Success", "Organisation Updated", result), HttpStatus.OK);

	}

	@GetMapping("/getAllOrganisation/{page}")
	public ResponseEntity<?> getAllOrganisation(@PathVariable("page") int page) throws BadRequestException {

		log.debug("Rest Request To get all Organisation.");

		return new ResponseEntity<>(
				new ResultStatus("Success", "Organisation Fetched", orgRepo.findAll(PageRequest.of(page, 10))),
				HttpStatus.OK);

	}

	@GetMapping("/OrganisationById/{id}")
	public ResponseEntity<?> getOrganisationById(@PathVariable("id") String id) throws BadRequestException {

		log.debug("Rest Request To get Organisation by id. ", id);

		return new ResponseEntity<>(new ResultStatus("Success", "Organisation Fetched", orgRepo.findById(id)),
				HttpStatus.OK);

	}

	@GetMapping("/OrganisationByVendor/{vendorId}")
	public ResponseEntity<?> getOrganisationByVendorId(@PathVariable("vendorId") String vendorId)
			throws BadRequestException {

		log.debug("Rest Request To get All Organisation by vendorId. ", vendorId);

		if (!userRepo.findById(vendorId).isPresent())
			throw new BadRequestException("user not present");

		return new ResponseEntity<>(
				new ResultStatus("Success", "Organisation Fetched", orgRepo.findAllByVendorId(vendorId)),
				HttpStatus.OK);

	}

	@GetMapping("/orgByAddress/{address}/{page}")
	public ResponseEntity<?> getOrganisationByVendorId(@PathVariable("address") String address,
			@PathVariable("page") int page) throws BadRequestException {

		log.debug("Rest Request To get All Organisation by address. ");

		return new ResponseEntity<>(
				new ResultStatus("Success", "Organisation Fetched",
						orgRepo.findAllByActivatedAndAddressLikeIgnoreCase(true, address, PageRequest.of(page, 10))),
				HttpStatus.OK);

	}

	@PutMapping("/orgByLatLong")
	public ResponseEntity<?> getOrganisationByLatLong(@RequestBody Filters filter) throws BadRequestException {

		log.debug("Rest Request To get All Organisation by Lat Long ");
		
		Sort sort = new Sort(Sort.Direction.DESC, filter.getSortCol());
		if (filter.getSortDir().equalsIgnoreCase("asc"))
			sort = new Sort(Sort.Direction.ASC, filter.getSortCol());

		return new ResponseEntity<>(new ResultStatus("Success", "Organisation Fetched",
				orgRepo.findAllByActivatedIsTrueAndLocationNear(new Point(filter.getLatitude(), filter.getLongitude()),
						new Distance(filter.getDistance(), Metrics.KILOMETERS), PageRequest.of(filter.getPage(), 10,sort))),
				HttpStatus.OK);

	}

	@GetMapping("/pendingOrganisation/{page}")
	public ResponseEntity<?> getPendingOrganisation(@PathVariable("page") int page) throws BadRequestException {

		log.debug("Rest Request To get All pending Organisation");

		return new ResponseEntity<>(new ResultStatus("Success", "Organisation Fetched",
				orgRepo.findAllByActivated(false, PageRequest.of(page, 10))), HttpStatus.OK);

	}

	@DeleteMapping("/organisation/{id}")
	public ResponseEntity<?> removeOrgannisation(@PathVariable("id") String id) {

		log.debug("rest request to remove id:", id);

		orgRepo.deleteById(id);

		return new ResponseEntity<>(new ResultStatus("Success", "Organisation Removed"), HttpStatus.OK);
	}

}
