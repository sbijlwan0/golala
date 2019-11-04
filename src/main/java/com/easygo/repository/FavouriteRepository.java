package com.easygo.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.easygo.domain.Favourite;

@Repository
public interface FavouriteRepository extends MongoRepository<Favourite,String>{
	
	Optional<Favourite> findByUserId(@Param("userId")String userId);

}
