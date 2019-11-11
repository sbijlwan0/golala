package com.easygo.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.easygo.domain.Address;

@Repository
public interface AddressRepository extends MongoRepository<Address,String> {
	
	List<Address> findAllByUserId(@Param("userId")String userId);

}
