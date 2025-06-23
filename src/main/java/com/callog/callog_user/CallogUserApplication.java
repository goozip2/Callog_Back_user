package com.callog.callog_user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;


@SpringBootApplication
@ConfigurationPropertiesScan
public class CallogUserApplication {

	public static void main(String[] args) {
		SpringApplication.run(CallogUserApplication.class, args);
	}

}
