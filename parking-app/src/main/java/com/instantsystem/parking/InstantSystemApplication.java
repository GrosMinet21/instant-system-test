package com.instantsystem.parking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class InstantSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(InstantSystemApplication.class, args);
	}

}
