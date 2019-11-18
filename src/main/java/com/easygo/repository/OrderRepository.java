package com.easygo.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.easygo.domain.Order;

@Repository
public interface OrderRepository extends MongoRepository<Order,String> {

	Page<Order> findAllByReturnOrder(@Param("returnOrder")boolean returnOrder, Pageable pageable);
	
	Page<Order> findByReturnOrderAndUserId(@Param("returnOrder")boolean returnOrder, @Param("userId")String userId, Pageable pageable);
	
	@Query("{ 'items': { $elemMatch: { 'productId' : {$in: ?0} } }}")
	Page<Order> findByProductIdIn(@Param("id")List<String>id,Pageable pageable);

}
