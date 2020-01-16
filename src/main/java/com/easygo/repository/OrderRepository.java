package com.easygo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.easygo.domain.Order;

@Repository
public interface OrderRepository extends MongoRepository<Order,String> {

	Page<Order> findAllByReturnOrder(@Param("returnOrder")boolean returnOrder, Pageable pageable);
	
	Optional<Order> findOneByOrderNo(@Param("orderNo")String orderNo);
	
	Page<Order> findByDelivererId(@Param("delivererId")String delivererId,Pageable pageable);
	
	Page<Order> findAllByOrgId(@Param("orgId")String orgId,Pageable pageable);
	
	Page<Order> findAllByOrgIdIn(@Param("orgIds")List<String> orgId,Pageable pageable);
	
	List<Order> findAllByRootOrderId(@Param("rootOrderId")String rootOrderId);
	
	List<Order> findByDriverAssignedAndStatusLikeIgnoreCaseAndLocationNear(@Param("driverAssigned") boolean driverAssigned,@Param("status")String status,
			@Param("point") Point point, @Param("d") Distance d,Sort sort);
	
	Page<Order> findByReturnOrderAndUserId(@Param("returnOrder")boolean returnOrder, @Param("userId")String userId, Pageable pageable);
	
	@Query("{ 'items': { $elemMatch: { 'productId' : {$in: ?0} } }}")
	Page<Order> findByProductIdIn(@Param("id")List<String>id,Pageable pageable);

}
