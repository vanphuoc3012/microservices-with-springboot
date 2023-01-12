package com.microservices.core.review;

import com.microservices.api.core.review.Review;
import com.microservices.core.review.persistence.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.junit.Assert.assertEquals;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ReviewServiceApplicationTests extends MySQLTestBase{

	@Autowired
	private WebTestClient client;

	@Autowired
	private ReviewRepository repository;

	@BeforeEach
	void setupDb() {
		repository.deleteAll();
	}

	@Test
	void getReviewByProductId() {
		int productId = 1;

		assertEquals(0, repository.count());
		
		postAndVerifyReview(productId, 1, OK);
		postAndVerifyReview(productId, 2, OK);
		postAndVerifyReview(productId, 3, OK);

		assertEquals(3, repository.count());

		getAndVerifyReviewsByProductId(productId, OK)
				.jsonPath("$.length()").isEqualTo(3)
				.jsonPath("$.[2].productId").isEqualTo(productId)
				.jsonPath("$.[2].reviewId").isEqualTo(3);
	}

	@Test
	void duplicateError() {
		int productId = 1;

		assertEquals(0, repository.count());

		postAndVerifyReview(productId, 1, OK);
		postAndVerifyReview(productId, 1, UNPROCESSABLE_ENTITY)
				.jsonPath("$.message").isEqualTo("Duplicate key, productId: " + productId + "Review Id: " + 1)
				.jsonPath("$.path").isEqualTo("/review");

		assertEquals(1, repository.count());
	}

	@Test
	void deleteReview() {
		int productId = 1;

		assertEquals(0, repository.count());

		postAndVerifyReview(productId, 1, OK);
		postAndVerifyReview(productId, 2, OK);
		postAndVerifyReview(productId, 3, OK);

		assertEquals(3, repository.findByProductId(productId).size());

		deleteAndVerifyReviewsByProductId(productId, OK);

		assertEquals(0, repository.count());

	}

	@Test
	void contextLoads() {
	}

	private WebTestClient.BodyContentSpec deleteAndVerifyReviewsByProductId(int productId, HttpStatus expectedStatus) {
		return client.delete()
				.uri("/review?productId=" + productId)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus)
				.expectBody();
	}

	private WebTestClient.BodyContentSpec postAndVerifyReview(int productId, int reviewId, HttpStatus expectedStatus) {
		Review review = new Review(productId, reviewId, "Author" + reviewId, "Subject" + reviewId, "Content" + reviewId);
		return client.post()
				.uri("/review")
				.accept(APPLICATION_JSON)
				.body(Mono.just(review), Review.class)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus)
				.expectHeader().contentType(APPLICATION_JSON)
				.expectBody();
	}

	private WebTestClient.BodyContentSpec getAndVerifyReviewsByProductId(int productId, HttpStatus expectedStatus) {
		return client.get()
				.uri("/review?productId=" + productId)
				.accept(APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus)
				.expectHeader().contentType(APPLICATION_JSON)
				.expectBody();
	}
}
