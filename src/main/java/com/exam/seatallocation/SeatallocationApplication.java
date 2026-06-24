package com.exam.seatallocation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SeatallocationApplication {
	public static void main(String[] args) {
		SpringApplication.run(SeatallocationApplication.class, args);
	}
}