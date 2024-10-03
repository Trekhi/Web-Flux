package com.tamara.webHook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class WebHookApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebHookApplication.class, args);
	}

}
