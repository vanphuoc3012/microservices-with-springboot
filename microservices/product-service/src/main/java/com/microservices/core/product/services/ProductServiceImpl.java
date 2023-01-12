package com.microservices.core.product.services;

import com.microservices.api.core.product.Product;
import com.microservices.api.core.product.ProductService;
import com.microservices.api.exception.InvalidInputException;
import com.microservices.api.exception.NotFoundException;
import com.microservices.core.product.persistence.ProductEntity;
import com.microservices.core.product.persistence.ProductRepository;
import com.microservices.util.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductServiceImpl implements ProductService {

    private static final Logger LOG = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ServiceUtil serviceUtil;

    private final ProductMapper mapper;

    private final ProductRepository repository;

    @Autowired
    public ProductServiceImpl(ServiceUtil serviceUtil, ProductMapper mapper, ProductRepository repository) {
        this.serviceUtil = serviceUtil;
        this.mapper = mapper;
        this.repository = repository;
    }


    @Override
    public Product getProduct(int productId) {
        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: "+productId);
        }
        ProductEntity entity = repository.findByProductId(productId).orElseThrow(() ->
            new NotFoundException("No product found for productId: " + productId));

        Product response = mapper.entityToApi(entity);
        response.setServiceAddress(serviceUtil.getServiceAddress());

        LOG.debug("getProduct: found productId: {}", response.getProductId());
        return response;
    }

    @Override
    public Product createProduct(Product body) {
        try {
            ProductEntity entity = mapper.apiToEntity(body);
            ProductEntity newEntity = repository.save(entity);

            LOG.debug("createProduct: entity created for productId: {}", body.getProductId());
            return mapper.entityToApi(newEntity);
        } catch (DuplicateKeyException dke) {
            throw new InvalidInputException("Duplicated key, Product Id: " + body.getProductId());
        }
    }

    @Override
    public void deleteProduct(int productId) {
        repository.findByProductId(productId).ifPresent(e ->
                repository.delete(e));

    }
}
