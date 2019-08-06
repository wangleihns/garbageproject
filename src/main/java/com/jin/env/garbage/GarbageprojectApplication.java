package com.jin.env.garbage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GarbageprojectApplication {
	private static Logger logger = LoggerFactory.getLogger(GarbageprojectApplication.class);
	public static void main(String[] args) {
		SpringApplication.run(GarbageprojectApplication.class, args);
		logger.info("GarbageprojectApplication start success ");
	}

}
