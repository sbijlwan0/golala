package com.easygo.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.easygo.domain.Wallet;

@Repository
public interface WalletRepository extends MongoRepository<Wallet,String> {
	
	Optional<Wallet> findOneByVendorId(@Param("vendorId")String vendorId);

}
