package com.easygo.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.easygo.domain.Cart;

@Repository
public interface CartRepository extends MongoRepository<Cart,String>{
	
	Optional<Cart> findByUserId(@Param("userId")String userId);

}
