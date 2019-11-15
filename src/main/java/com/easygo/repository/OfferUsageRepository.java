package com.easygo.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.easygo.domain.OfferUsage;

@Repository
public interface OfferUsageRepository extends MongoRepository<OfferUsage,String> {
	
	Optional<OfferUsage> findOneByOfferIdAndUserId(@Param("offerId")String offerId,@Param("userId")String userId);
	

}
