package com.easygo.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.easygo.domain.TrackingDetails;


@Repository
public interface TrackingDetailsRepository extends MongoRepository<TrackingDetails,String>{
	
	Optional<TrackingDetails> findOneByDriverId(@Param("driverId") String driverId);

}
