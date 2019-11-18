package com.easygo.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.easygo.domain.Product;

@Repository
public interface ProductRepository extends MongoRepository<Product,String>{
	
	Page<Product> findAllByOrganisationId(@Param("organisationId")String orgId,Pageable pageable);
	
	Page<Product> findAllByActiveIsTrueAndOrganisationId(@Param("organisationId")String orgId,Pageable pageable);
	
	Page<Product> findAllByActiveIsTrueAndCategory(@Param("category")String category,Pageable pageable);
	
	Page<Product> findAllByActiveIsTrueAndCategoryAndOrganisationIdIn(@Param("category")String category,@Param("ids")List<String> ids,Pageable pageable);
	
	Page<Product> findAllByActiveIsTrueAndOrganisationIdIn(@Param("ids")List<String> ids,Pageable pageable);
	
//	@Query(value="{ organisationId : ?0}", fields="{ id : 1 }")
//	List<String> findAllByOrganisationId(@Param("id") String id);
}
