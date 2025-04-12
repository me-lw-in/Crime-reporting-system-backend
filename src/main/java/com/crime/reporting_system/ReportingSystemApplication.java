package com.crime.reporting_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.crime.reporting_system")
public class ReportingSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReportingSystemApplication.class, args);
		System.out.println("hello main");
	}

}
