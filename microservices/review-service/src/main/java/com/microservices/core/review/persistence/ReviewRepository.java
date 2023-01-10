package com.microservices.core.review.persistence;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface ReviewRepository extends CrudRepository<ReviewEntity, String> {

    @Transactional(readOnly = true)
    Optional<ReviewEntity> findByProductId(int productId);
}
