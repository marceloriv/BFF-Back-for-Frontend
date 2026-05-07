package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.example.demo.dto.usuario.UsuarioRequest;
import com.example.demo.dto.usuario.UsuarioResponse;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioBffController {

	private final RestTemplate restTemplate;
	private final String gatewayBaseUrl;

	public UsuarioBffController(RestTemplate restTemplate,
			@Value("${gateway.base-url:http://localhost:8222}") String gatewayBaseUrl) {
		this.restTemplate = restTemplate;
		this.gatewayBaseUrl = gatewayBaseUrl;
	}

	@PostMapping
	public ResponseEntity<UsuarioResponse> registrarUsuario(@RequestBody UsuarioRequest usuario,
			@RequestHeader HttpHeaders headers) {
		HttpHeaders forwardedHeaders = new HttpHeaders();
		forwardedHeaders.putAll(headers);
		forwardedHeaders.remove(HttpHeaders.HOST);
		forwardedHeaders.remove(HttpHeaders.CONTENT_LENGTH);

		HttpEntity<UsuarioRequest> requestEntity = new HttpEntity<>(usuario, forwardedHeaders);
		ResponseEntity<UsuarioResponse> response = restTemplate.exchange(
				gatewayBaseUrl + "/api/v1/usuarios",
				HttpMethod.POST,
				requestEntity,
				UsuarioResponse.class);

		return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
	}

}
