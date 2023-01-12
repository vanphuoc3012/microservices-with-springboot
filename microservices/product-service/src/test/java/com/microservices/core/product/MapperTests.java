package com.microservices.core.product;

import com.microservices.api.core.product.Product;
import com.microservices.core.product.persistence.ProductEntity;
import com.microservices.core.product.services.ProductMapper;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MapperTests {

    private ProductMapper mapper = Mappers.getMapper(ProductMapper.class);

    @Test
    void mapperTest() {
        assertNotNull(mapper);

        Product api = new Product(1, "n", 1, "sa");

        ProductEntity entity = mapper.apiToEntity(api);

        assertEquals(api.getName(), entity.getName());
    }
}
