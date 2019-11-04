package com.easygo.repository;

import com.easygo.domain.Authority;
import com.easygo.domain.User;

import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.time.Instant;

/**
 * Spring Data MongoDB repository for the {@link User} entity.
 */
@Repository
public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findOneByActivationKey(String activationKey);

    List<User> findAllByActivatedIsFalseAndCreatedDateBefore(Instant dateTime);

    Optional<User> findOneByResetKey(String resetKey);

    Optional<User> findOneByEmailIgnoreCase(String email);
    
    Optional<User> findOneByMobile(String mobile);

    Optional<User> findOneByLogin(String login);
    
    Page<User> findAllByAuthoritiesContains(Authority auth,Pageable pageable);
    
    Page<User> findAllByActivatedIsFalse(Pageable pageable);

    Page<User> findAllByLoginNot(Pageable pageable, String login);
}
