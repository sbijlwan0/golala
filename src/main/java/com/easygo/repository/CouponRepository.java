package com.easygo.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.easygo.domain.Coupon;

@Repository
public interface CouponRepository extends MongoRepository<Coupon,String>{
	
	Optional<Coupon> findByCode(@Param("code")String code);

}
