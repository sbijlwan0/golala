package com.easygo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Sort;

import com.easygo.domain.Payment;

@Repository
public interface PaymentRepository extends MongoRepository<Payment,String> {
	
	Optional<Payment> findOneByRootOrderIdAndSuccessIsTrue(@Param("rootOrderId")String rootOrderId);
	
	List<Payment> findAllByRootOrderId(@Param("rootOrderId")String rootOrderId,Sort sort);
	
	List<Payment> findByUserId(@Param("userId")String userId);

}
