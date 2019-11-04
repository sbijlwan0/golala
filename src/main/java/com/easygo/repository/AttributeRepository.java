package com.easygo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.easygo.domain.Attribute;

@Repository
public interface AttributeRepository extends MongoRepository<Attribute,String>{

}
