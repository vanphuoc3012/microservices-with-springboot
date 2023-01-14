package com.microservices.api.core.product;

import com.microservices.api.composite.product.ProductAggregate;

public class Product {

    private int productId;

    private String name;

    private int weight;

    private String serviceAddress;

    public Product(int productId, String name, int weight, String serviceAddress) {
        this.productId = productId;
        this.name = name;
        this.weight = weight;
        this.serviceAddress = serviceAddress;
    }

    public Product() {
        productId = 0;
        name = null;
        weight = 0;
        serviceAddress = null;
    }

    public int getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public int getWeight() {
        return weight;
    }

    public String getServiceAddress() {
        return serviceAddress;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public void setServiceAddress(String serviceAddress) {
        this.serviceAddress = serviceAddress;
    }

    public static Product getProductFromAggregate(ProductAggregate aggregate) {
        Product product = new Product(
                aggregate.getProductId(),
                aggregate.getName(),
                aggregate.getWeight(),
                null);
        return product;
    }
}
