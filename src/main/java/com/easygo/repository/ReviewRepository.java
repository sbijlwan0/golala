package com.easygo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.easygo.domain.Review;

@Repository
public interface ReviewRepository extends MongoRepository<Review,String> {
	
	Optional<Review> findOneByUserIdAndItemIdAndType(@Param("userId")String userId,@Param("itemId")String itemId,@Param("type")String type);
	
	List<Review> findAllByItemIdAndType(@Param("itemId")String itemId,@Param("type")String type);
	
	

}
