package com.easygo.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.easygo.domain.Category;

@Repository
public interface CategoryRepository extends MongoRepository<Category,String> {

	List<Category> findAllByParentCategory(@Param("parentCategory")String parentCategory);
	
}
