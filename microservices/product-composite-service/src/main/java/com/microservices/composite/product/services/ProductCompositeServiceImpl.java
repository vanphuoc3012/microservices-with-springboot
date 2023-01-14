package com.microservices.composite.product.services;

import com.microservices.api.composite.product.*;
import com.microservices.api.core.product.Product;
import com.microservices.api.core.recommendation.Recommendation;
import com.microservices.api.core.review.Review;
import com.microservices.util.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ProductCompositeServiceImpl implements ProductCompositeService {

    private final ServiceUtil serviceUtil;

    private ProductCompositeIntegration integration;

    private final Logger LOG = LoggerFactory.getLogger(ProductCompositeServiceImpl.class);

    @Autowired
    public ProductCompositeServiceImpl(ServiceUtil serviceUtil, ProductCompositeIntegration integration) {
        this.serviceUtil = serviceUtil;
        this.integration = integration;
    }

    @Override
    public ProductAggregate getProduct(int productId) {

        Product product = integration.getProduct(productId);
        List<Recommendation> recommendations = integration.getRecommendations(productId);
        List<Review> reviews = integration.getReviews(productId);
        return createProductAggregate(product, recommendations, reviews, serviceUtil.getServiceAddress());
    }

    @Override
    public void createProduct(ProductAggregate body) {
        try {
            Product product = Product.getProductFromAggregate(body);
            integration.createProduct(product);

            if(body.getRecommendations() != null) {
                body.getRecommendations().forEach(recSummary -> {
                    Recommendation recommendation =
                            Recommendation.recommendationFromRecommendationSummary(body.getProductId(), recSummary);
                    integration.createRecommendation(recommendation);
                });
            }

            if(body.getReviews() != null) {
                body.getReviews().forEach(reviewSummary -> {
                    Review review = Review.reviewSummaryToReview(body.getProductId(), reviewSummary);
                    integration.createReview(review);
                });
            }
        } catch (RuntimeException ex) {
            LOG.warn("createCompositeProduct fail", ex);
            throw ex;
        }
    }

    @Override
    public void deleteProduct(int productId) {
        integration.deleteProduct(productId);
        integration.deleteReview(productId);
        integration.deleteRecommendation(productId);
    }

    private ProductAggregate createProductAggregate(Product product,
                                                   List<Recommendation> recommendations,
                                                   List<Review> reviews,
                                                   String serviceAddress) {
        //1.product information
        int productId = product.getProductId();
        String name = product.getName();
        int weight = product.getWeight();

        //2.recommendationSummary if any
        List<RecommendationSummary> recommendationSummaries =
                (recommendations == null) ? null : recommendations.stream()
                        .map(r -> new RecommendationSummary(r.getRecommendationId(), r.getAuthor(), r.getRate(), r.getContent()))
                        .collect(Collectors.toList());

        //3.reviewSummary list if any
        List<ReviewSummary> reviewSummaries =
                (reviews == null) ? null : reviews.stream()
                        .map(r -> new ReviewSummary(r.getReviewId(), r.getAuthor(), r.getSubject(), r.getContent()))
                        .collect(Collectors.toList());

        //4. Create ServiceAddresses, info regarding the involved microservices addresses
        String productAddress = product.getServiceAddress();
        String reviewAddress = (reviews != null && reviews.size() > 0) ? reviews.get(0).getServiceAddress() : "";
        String recommendationAddress = (recommendations != null && recommendations.size() > 0) ? recommendations.get(0).getServiceAddress() : "";
        ServiceAddresses serviceAddresses = new ServiceAddresses(serviceAddress, productAddress, reviewAddress, recommendationAddress);

        return new ProductAggregate(
                productId,
                name,
                weight,
                recommendationSummaries,
                reviewSummaries,
                serviceAddresses
        );
    }
}
