package com.ticketti.bff.controller;

import com.ticketti.bff.service.ApiGatewayService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
// Controlador que actúa como proxy para reenviar las solicitudes a la API Gateway
@RestController
@RequestMapping("/api")
public class GatewayProxyController {

	private final ApiGatewayService apiGatewayService;

	public GatewayProxyController(ApiGatewayService apiGatewayService) {
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

	// Multipart (subida de archivos) no puede leerse como @RequestBody String:
	// eso corrompe el binario y rompe el boundary. Spring elige este método
	// sobre reenviarPost() cuando el Content-Type es multipart/form-data.
	@PostMapping(value = "/{microservicio}/**", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public String reenviarPostMultipart(
			@PathVariable String microservicio,
			HttpServletRequest request
	) {
		return apiGatewayService.reenviarPostMultipart(microservicio, request);
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
