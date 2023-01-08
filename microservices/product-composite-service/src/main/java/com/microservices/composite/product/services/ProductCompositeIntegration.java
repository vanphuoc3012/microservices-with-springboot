package com.microservices.composite.product.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.api.core.product.Product;
import com.microservices.api.core.product.ProductService;
import com.microservices.api.core.recommendation.Recommendation;
import com.microservices.api.core.recommendation.RecommendationService;
import com.microservices.api.core.review.Review;
import com.microservices.api.core.review.ReviewService;
import com.microservices.api.exception.InvalidInputException;
import com.microservices.api.exception.NotFoundException;
import com.microservices.util.HttpErrorInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
@Component
public class ProductCompositeIntegration implements ProductService, RecommendationService, ReviewService {

    private final Logger LOG = LoggerFactory.getLogger(ProductCompositeIntegration.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private final String productServiceUrl;
    private final String recommendationServiceUrl;
    private final String reviewServiceUrl;

    @Autowired
    public ProductCompositeIntegration(RestTemplate restTemplate, ObjectMapper objectMapper,
                                       @Value("localhost") String productServiceHost,
                                       @Value("7001") int productServicePort,
                                       @Value("localhost") String recommendationServiceHost,
                                       @Value("7002") int recommendationServicePort,
                                       @Value("localhost") String reviewServiceHost,
                                       @Value("7001") int reviewServicePort) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;

        this.productServiceUrl = "http://"+productServiceHost + ":" + productServicePort + "/product/";
        this.recommendationServiceUrl = "http://"+recommendationServiceHost + ":" + recommendationServicePort + "/recommendation?productId=";
        this.reviewServiceUrl = "http://" + reviewServiceHost + ":" + reviewServicePort + "/review?productId=";
    }

    @Override
    public Product getProduct(int productId) {
        try {
            String url = productServiceUrl + productId;
            LOG.debug("Will call getProduct API on url: " + url);

            Product product = restTemplate.getForObject(url, Product.class);
            LOG.debug("Found a product with id: {}" + productId);
            return product;
        } catch (HttpClientErrorException ex) {
            switch (ex.getStatusCode()) {
                case NOT_FOUND:
                    throw new NotFoundException(getErrorMessage(ex));
                case UNPROCESSABLE_ENTITY:
                    throw new InvalidInputException(getErrorMessage(ex));
                default:
                    LOG.warn("Got an unexpected HTTP error: {}, will rethrow it.", ex.getStatusCode());
                    LOG.warn("Error body: {}", ex.getResponseBodyAsString());
                    throw ex;
            }
        }
    }

    private String getErrorMessage(HttpClientErrorException ex) {
        try {
            return objectMapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (IOException e) {
            return e.getMessage();
        }
    }

    @Override
    public List<Recommendation> getRecommendations(int productId) {
        try {
            String url = recommendationServiceUrl + productId;
            LOG.debug("Will call getRecommendations API on url: {}", url);

            List<Recommendation> list = restTemplate.
                    exchange(url, HttpMethod.GET,
                    null, new ParameterizedTypeReference<List<Recommendation>>() {}).getBody();
            LOG.debug("Found {} recommendations for a product with id: {}", list.size(), productId);

            return list;
        } catch (Exception e) {
            LOG.warn("Got an exception while requesting recommendations, return zero recommendations: {}", e.getMessage());

            return new ArrayList<>();
        }
    }

    @Override
    public List<Review> getReviews(int productId) {
        try {
            String url = reviewServiceUrl + productId;
            LOG.debug("Will call getReviews API on url: {}", url);

            List<Review> list = restTemplate.exchange(url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<List<Review>>(){}).getBody();
            LOG.debug("Found {} reviews for a product with id: {}", list.size(), productId);

            return list;
        } catch (Exception ex) {
            LOG.warn("Got an exception while requesting reviews, return zero reviews: {}", ex.getMessage());

            return new ArrayList<>();
        }
    }
}
