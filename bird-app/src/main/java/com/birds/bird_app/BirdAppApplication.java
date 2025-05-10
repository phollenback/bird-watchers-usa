package com.birds.bird_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BirdAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(BirdAppApplication.class, args);
	}

}
