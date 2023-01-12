package com.microservices.core.recommendation.services;

import com.microservices.api.core.recommendation.Recommendation;
import com.microservices.api.core.recommendation.RecommendationService;
import com.microservices.api.exception.InvalidInputException;
import com.microservices.core.recommendation.persistence.RecommendationEntity;
import com.microservices.core.recommendation.persistence.RecommendationRepository;
import com.microservices.util.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class RecommendationServicesImpl implements RecommendationService {

    private final Logger LOG = LoggerFactory.getLogger(RecommendationServicesImpl.class);

    private final RecommendationRepository repository;

    private final ServiceUtil serviceUtil;

    private final RecommendationMapper mapper;

    @Autowired
    public RecommendationServicesImpl(RecommendationRepository repository, ServiceUtil serviceUtil, RecommendationMapper mapper) {
        this.repository = repository;
        this.serviceUtil = serviceUtil;
        this.mapper = mapper;
    }

    @Override
    public List<Recommendation> getRecommendations(int productId) {
        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        List<RecommendationEntity> entityList = repository.findByProductId(productId);
        List<Recommendation> list = mapper.entityListToApiList(entityList);
        list.forEach(e -> e.setServiceAddress(serviceUtil.getServiceAddress()));

        LOG.debug("getRecommendations: response size: {}", list.size());

        return list;
    }

    @Override
    public Recommendation createRecommendation(Recommendation body) {
        try {
            RecommendationEntity entity = mapper.apiToEntity(body);
            RecommendationEntity newEntity = repository.save(entity);

            LOG.debug("createRecommendation: created a recommendation entity: {}/{}", body.getProductId(), body.getRecommendationId());
            return mapper.entityToApi(newEntity);

        } catch (DuplicateKeyException dke) {
            throw new InvalidInputException("Duplicate key, Product Id: " + body.getProductId() + ", Recommendation Id:" + body.getRecommendationId());
        }
    }

    @Override
    public void deleteRecommendation(int productId) {
        LOG.debug("deleteRecommendations: tries to delete recommendations for the product with productId: {}", productId);
        repository.deleteAll(repository.findByProductId(productId));
    }
}
