package com.microservices.core.review.services;

import com.microservices.api.core.review.Review;
import com.microservices.api.core.review.ReviewService;
import com.microservices.api.exception.InvalidInputException;
import com.microservices.core.review.persistence.ReviewEntity;
import com.microservices.core.review.persistence.ReviewRepository;
import com.microservices.util.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ReviewServiceImpl implements ReviewService {

    private final Logger LOG = LoggerFactory.getLogger(ReviewServiceImpl.class);

    private final ServiceUtil serviceUtil;

    private final ReviewRepository repository;

    private final ReviewMapper mapper;

    @Autowired
    public ReviewServiceImpl(ServiceUtil serviceUtil, ReviewRepository repository, ReviewMapper mapper) {
        this.serviceUtil = serviceUtil;
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public List<Review> getReviews(int productId) {
        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        List<ReviewEntity> reviewList = repository.findByProductId(productId);
        List<Review> apiList = mapper.entityListToApiList(reviewList);
        apiList.forEach(review -> review.setServiceAddress(serviceUtil.getServiceAddress()));

        LOG.debug("getReview: reviews response size: {}", apiList.size());
        return apiList;
    }

    @Override
    public Review createReview(Review body) {
        try {
            ReviewEntity entity = mapper.apiToEntity(body);
            ReviewEntity savedEntity = repository.save(entity);

            LOG.debug("createReview: created a review entity (productId/reviewId): {}/{}", savedEntity.getProductId(), savedEntity.getReviewId());
            return mapper.entityToApi(savedEntity);
        } catch (DataIntegrityViolationException dive) {
            throw new InvalidInputException("Duplicate key, productId: " + body.getProductId() + "Review Id: " + body.getReviewId());
        }
    }

    @Override
    public void deleteReview(int productId) {
        LOG.debug("deleteReview: delete all reviews of product Id: {}", productId);
        List<ReviewEntity> reviews = repository.findByProductId(productId);
        repository.deleteAll(reviews);
    }
}
