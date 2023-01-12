package com.microservices.core.product;

import com.microservices.api.core.product.Product;
import com.microservices.core.product.persistence.ProductRepository;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductServiceApplicationTests extends MongoDBTestBase {

	@Autowired private WebTestClient client;

	@Autowired private ProductRepository repository;

	@BeforeEach
	void setupDb () {
		repository.deleteAll();
	}

	@Test
	void getProductById() {
		int productId = 1;

		postAndVerifyProduct(productId, OK);

		assertTrue(repository.findByProductId(productId).isPresent());

		getAndVerifyProduct(productId, OK);
	}

	@Test
	void duplicatedError() {
		int productId = 1;

		postAndVerifyProduct(productId, OK);

		assertTrue(repository.findByProductId(productId).isPresent());

		postAndVerifyProduct(productId, UNPROCESSABLE_ENTITY)
				.jsonPath("$.path").isEqualTo("/product")
				.jsonPath("$.message").isEqualTo("Duplicated key, Product Id: " + productId);
	}

	@Test
	void deleteProduct() {
		int productId = 1;

		postAndVerifyProduct(productId, OK);
		assertTrue(repository.findByProductId(productId).isPresent());

		deleteAndVerifyProduct(productId, OK);
		assertFalse(repository.findByProductId(productId).isPresent());

		deleteAndVerifyProduct(productId, OK);
	}

	@Test
	void getProductInvalidParam() {
		getAndVerifyProduct("/no-productId", BAD_REQUEST)
				.jsonPath("$.path").isEqualTo("/product/no-productId")
				.jsonPath("$.message").isEqualTo("Type mismatch.");
	}

	@Test
	void getProductNotFound() {
		int productId = 13;
		getAndVerifyProduct(productId, NOT_FOUND)
				.jsonPath("$.path").isEqualTo("/product/13")
				.jsonPath("$.message").isEqualTo("No product found for productId: " + productId);
	}

	@Test
	void getProductInvalidParameterNegativeValue() {
		int productId = -1;
		getAndVerifyProduct(productId, UNPROCESSABLE_ENTITY)
				.jsonPath("$.path").isEqualTo("/product/-1")
				.jsonPath("$.message").isEqualTo("Invalid productId: "+productId);
	}
	@Test
	void contextLoads() {
	}

	private WebTestClient.BodyContentSpec getAndVerifyProduct(
			int productId, HttpStatus expectedStatus) {
		return getAndVerifyProduct("/" + productId, expectedStatus);
	}

	private WebTestClient.BodyContentSpec getAndVerifyProduct(
			String productIdPath, HttpStatus expectedStatus) {
		return client.get()
				.uri("/product" + productIdPath)
				.accept(APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus)
				.expectHeader().contentType(APPLICATION_JSON)
				.expectBody();
	}

	private WebTestClient.BodyContentSpec deleteAndVerifyProduct(int productId, HttpStatus expectedStatus) {
		return client.delete()
				.uri("/product/" + productId)
				.accept(APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(OK)
				.expectBody();
	}

	private WebTestClient.BodyContentSpec postAndVerifyProduct(
			int productId, HttpStatus expectedStatus) {
		Product product = new Product(productId, "Name" + productId, productId, "SA");

		return client.post()
				.uri("/product")
				.body(Mono.just(product), Product.class)
				.accept(APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus)
				.expectHeader().contentType(APPLICATION_JSON)
				.expectBody();
	}

}
