package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

	@Bean
    // Configura un RestTemplate para hacer llamadas HTTP a otros servicios
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

}
