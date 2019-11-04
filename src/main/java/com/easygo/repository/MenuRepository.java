package com.easygo.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.easygo.domain.Menu;

@Repository
public interface MenuRepository extends MongoRepository<Menu,String>{
	
	Optional<Menu> findByOrgId(@Param("orgId")String orgId);

}
