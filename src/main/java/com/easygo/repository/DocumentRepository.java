package com.easygo.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.easygo.domain.Document;

@Repository
public interface DocumentRepository extends MongoRepository<Document,String> {
	
	Optional<Document> findOneByUserId(@Param("userId") String userId);

}
