package com.easygo.web.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.easygo.domain.Wallet;
import com.easygo.repository.WalletRepository;
import com.easygo.service.dto.ResultStatus;

@RestController
@RequestMapping("/api")
public class WalletResource {
	
	private final Logger log = LoggerFactory.getLogger(WalletResource.class);
	
	@Autowired
	WalletRepository walletRepo;
	
	
	@GetMapping("/wallets")
	public ResponseEntity<?> getAllWallets(){
		log.debug("rest request to get all wallets");
		
		return new ResponseEntity<>(new ResultStatus("Success","Wallets Fetched",walletRepo.findAll()),HttpStatus.OK);
	}
	
	
	@GetMapping("/wallet/{id}")
	public ResponseEntity<?> getAllWalletById(@PathVariable("id")String id){
		log.debug("rest request to get wallet by id",id);
		
		return new ResponseEntity<>(new ResultStatus("Success","Wallet Fetched",walletRepo.findById(id).get()),HttpStatus.OK);
	}
	
	
	@GetMapping("/walletByUserId/{id}")
	public ResponseEntity<?> getAllWalletByVendorId(@PathVariable("id")String id){
		log.debug("rest request to get wallet by vendorId",id);
		
		Wallet wal=new Wallet();
		if(!walletRepo.findOneByVendorId(id).isPresent())
			wal=walletRepo.findOneByVendorId(id).get();
		
		wal.setVendorId(id);
		walletRepo.save(wal);
		
		return new ResponseEntity<>(new ResultStatus("Success","Wallet Fetched",wal),HttpStatus.OK);
	}

}
