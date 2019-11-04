package com.easygo.web.rest;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.easygo.domain.Favourite;
import com.easygo.domain.Organisation;
import com.easygo.repository.FavouriteRepository;
import com.easygo.repository.OrganisationRepository;
import com.easygo.repository.UserRepository;
import com.easygo.security.SecurityUtils;
import com.easygo.service.dto.ResultStatus;

import io.undertow.util.BadRequestException;

@RestController
@RequestMapping("/api")
public class FavouriteResource {

	private final Logger log = LoggerFactory.getLogger(FavouriteResource.class);

	@Autowired
	FavouriteRepository favRepo;

	@Autowired
	OrganisationRepository orgRepo;

	@Autowired
	UserRepository userRepo;

	@GetMapping("favourite/{orgId}")
	public ResponseEntity<?> addRemoveFavourite(@PathVariable("orgId") String orgId) throws BadRequestException {

		log.debug("add remove organisation from favourite");

		if (!orgRepo.findById(orgId).isPresent())
			throw new BadRequestException("Invalid Organisation ");

		Organisation org = orgRepo.findById(orgId).get();

		if (!userRepo.findOneByLogin(SecurityUtils.getCurrentUserLogin().get()).isPresent())
			throw new BadRequestException("Please Login First");

		if (!favRepo.findByUserId(userRepo.findOneByLogin(SecurityUtils.getCurrentUserLogin().get()).get().getId())
				.isPresent()) {
			Favourite fav = new Favourite();
			List<Organisation> orgs = new ArrayList<Organisation>();
			fav.setUserId(userRepo.findOneByLogin(SecurityUtils.getCurrentUserLogin().get()).get().getId());
			orgs.add(org);
			fav.setOrganisations(orgs);
			Favourite result = favRepo.save(fav);
			return new ResponseEntity<>(new ResultStatus("success", "Favourite Added", result), HttpStatus.OK);
		} else {

			Favourite fav = favRepo
					.findByUserId(userRepo.findOneByLogin(SecurityUtils.getCurrentUserLogin().get()).get().getId())
					.get();
			if (fav.getOrganisations().contains(org)) {
				fav.getOrganisations().remove(org);
				Favourite result = favRepo.save(fav);
				return new ResponseEntity<>(new ResultStatus("success", "Favourite Removed", result), HttpStatus.OK);
			} else {
				fav.getOrganisations().add(org);
				Favourite result = favRepo.save(fav);
				return new ResponseEntity<>(new ResultStatus("success", "Favourite Added", result), HttpStatus.OK);
			}
		}

	}

	@GetMapping("favouriteByUserId")
	public ResponseEntity<?> getFavourite() throws BadRequestException {

		log.debug("rest request to get favourite by user id");

		if (!userRepo.findOneByLogin(SecurityUtils.getCurrentUserLogin().get()).isPresent())
			throw new BadRequestException("Please Login First");

		if (!favRepo.findByUserId(userRepo.findOneByLogin(SecurityUtils.getCurrentUserLogin().get()).get().getId())
				.isPresent()) {
			Favourite fav = new Favourite();
			List<Organisation> orgs = new ArrayList<Organisation>();
			fav.setUserId(userRepo.findOneByLogin(SecurityUtils.getCurrentUserLogin().get()).get().getId());
			fav.setOrganisations(orgs);
			Favourite result = favRepo.save(fav);
			return new ResponseEntity<>(new ResultStatus("success", "Favourite Fetched", result), HttpStatus.OK);
		}
		return new ResponseEntity<>(new ResultStatus("success", "Favourite Fetched", favRepo
				.findByUserId(userRepo.findOneByLogin(SecurityUtils.getCurrentUserLogin().get()).get().getId()).get()),
				HttpStatus.OK);
	}

}
