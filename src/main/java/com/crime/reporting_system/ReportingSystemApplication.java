package com.crime.reporting_system;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.crime.reporting_system")
public class ReportingSystemApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure().load(); // Load .env file
		System.out.println("DB_HOST: " + dotenv.get("DB_HOST"));
		System.out.println("DB_PORT: " + dotenv.get("DB_PORT"));
		SpringApplication.run(ReportingSystemApplication.class, args);
		System.out.println("hello main");
	}

}
