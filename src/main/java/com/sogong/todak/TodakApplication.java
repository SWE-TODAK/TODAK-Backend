package com.sogong.todak;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class TodakApplication {

	public static void main(String[] args) {
		SpringApplication.run(TodakApplication.class, args);
	}

}
