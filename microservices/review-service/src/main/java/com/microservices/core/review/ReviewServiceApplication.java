package com.microservices.core.review;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

@SpringBootApplication
@ComponentScan("com.microservices")
public class ReviewServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReviewServiceApplication.class, args);
	}

}
