package com.example.demo.controller;

import com.example.demo.service.ApiGatewayService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class GatawayProxiController {

	private final ApiGatewayService apiGatewayService;

	public GatawayProxiController(ApiGatewayService apiGatewayService) {
		this.apiGatewayService = apiGatewayService;
	}

	@GetMapping("/{microservicio}/**")
	public String reenviarGet(@PathVariable String microservicio, HttpServletRequest request) {
		return apiGatewayService.reenviarGet(microservicio, request);
	}

	@PostMapping("/{microservicio}/**")
	public String reenviarPost(
			@PathVariable String microservicio,
			@RequestBody(required = false) String body,
			HttpServletRequest request
	) {
		return apiGatewayService.reenviarPost(microservicio, body, request);
	}

	@PutMapping("/{microservicio}/**")
	public String reenviarPut(
			@PathVariable String microservicio,
			@RequestBody(required = false) String body,
			HttpServletRequest request
	) {
		return apiGatewayService.reenviarPut(microservicio, body, request);
	}

	@DeleteMapping("/{microservicio}/**")
	public String reenviarDelete(@PathVariable String microservicio, HttpServletRequest request) {
		return apiGatewayService.reenviarDelete(microservicio, request);
	}
}
