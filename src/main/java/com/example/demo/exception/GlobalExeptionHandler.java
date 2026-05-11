package com.example.demo.exception;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExeptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
	    IllegalArgumentException exception,
	    HttpServletRequest request
    ) {
	ErrorResponse errorResponse = new ErrorResponse(
		LocalDateTime.now(),
		HttpStatus.BAD_REQUEST.value(),
		HttpStatus.BAD_REQUEST.getReasonPhrase(),
		exception.getMessage(),
		request.getRequestURI()
	);

	return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

	@ExceptionHandler(RestClientException.class)
	public ResponseEntity<ErrorResponse> handleRestClientException(
		RestClientException exception,
		HttpServletRequest request
	) {
	String detail = exception.getMessage() != null ? exception.getMessage() : "No se pudo conectar con el servicio externo";
	ErrorResponse errorResponse = new ErrorResponse(
		LocalDateTime.now(),
		HttpStatus.BAD_GATEWAY.value(),
		HttpStatus.BAD_GATEWAY.getReasonPhrase(),
		detail,
		request.getRequestURI()
	);

	return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(errorResponse);
	}

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
	    Exception exception,
	    HttpServletRequest request
    ) {
	ErrorResponse errorResponse = new ErrorResponse(
		LocalDateTime.now(),
		HttpStatus.INTERNAL_SERVER_ERROR.value(),
		HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
		exception.getMessage() != null ? exception.getMessage() : "Error inesperado",
		request.getRequestURI()
	);

	return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

}


