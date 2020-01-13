package com.easygo.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.easygo.domain.SequenceGenerator;

@Repository
public interface SequenceGeneratorRepository extends MongoRepository<SequenceGenerator,String>{
	
	Optional<SequenceGenerator> findOneByType(@Param("type")String type);

}
