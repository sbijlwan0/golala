package com.easygo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.easygo.domain.AttributeSet;

@Repository
public interface AttributeSetRepository extends MongoRepository<AttributeSet,String>{

}
