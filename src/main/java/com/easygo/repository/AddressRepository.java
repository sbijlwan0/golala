package com.easygo.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.easygo.domain.AddressDTO;

@Repository
public interface AddressRepository extends MongoRepository<AddressDTO,String> {
	
	List<AddressDTO> findAllByUserId(@Param("userId")String userId);

}
