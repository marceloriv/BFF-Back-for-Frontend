# BFF Back for Frontend

Backend tipo BFF para servir de capa intermedia entre un frontend y servicios de autenticación/usuario.

## Descripción

Este proyecto está construido con Spring Boot y Java 17. La estructura actual está pensada para centralizar la lógica de acceso, seguridad y consumo de servicios externos desde una sola API.

En esta base ya están creados los paquetes para:

- autenticación
- configuración de clientes HTTP
- seguridad con JWT
- DTOs de auth y usuario
- manejo global de errores


## Estructura del bff

src/main/java/com/example/demo
├─ client
│  └─ UsuarioClient.java
│
├─ config
│  ├─ RestTemplateConfig.java
│  └─ WebClientConfig.java
│
├─ controller
│  ├─ AuthController.java
│  └─ UsuarioBffController.java
│
├─ dto
│  ├─ auth
│  │  ├─ LoginRequest.java
│  │  └─ LoginResponse.java
│  │
│  └─ usuario
│     ├─ UsuarioRequest.java
│     └─ UsuarioResponse.java
│
├─ exception
│  ├─ ErrorResponse.java
│  └─ GlobalExceptionHandler.java
│
├─ security
│  ├─ JwtAuthenticationFilter.java
│  └─ SecurityConfig.java
│
└─ service
   ├─ AuthService.java
   ├─ JwtService.java
   └─ UsuarioBffService.java

## Stack

- Java 17
- Spring Boot 4
- Maven
- Lombok

## Estructura

- `src/main/java/com/example/demo`: aplicación principal
- `src/main/java/com/example/demo/controller`: controladores y capa BFF
- `src/main/java/com/example/demo/service`: lógica de negocio
- `src/main/java/com/example/demo/client`: clientes hacia servicios externos
- `src/main/java/com/example/demo/config`: configuración de beans y clientes HTTP
- `src/main/java/com/example/demo/dto`: modelos de entrada y salida
- `src/main/java/com/example/demo/exception`: manejo de errores
- `src/main/java/com/example/demo/secutiry`: configuración de seguridad

## Requisitos

- Java 17 o superior
- Maven 3.9 o superior

## Ejecución local

1. Compila el proyecto:

```bash
./mvnw clean install
```

2. Levanta la aplicación:

```bash
./mvnw spring-boot:run
```

En Windows también puedes usar:

```powershell
.\mvnw.cmd spring-boot:run
```

La aplicación arranca por defecto en el puerto `8080`.

## Configuración

La configuración actual vive en `src/main/resources/application.properties`.

```properties
spring.application.name=BFF-Back-for-Frontend
server.port=8080
```

## Estado actual

El proyecto tiene implementada la autenticación con JWT. La base estructural está lista con los clientes HTTP configurados. Actualmente se trabaja en completar los endpoints y conectar los servicios externos.

## Próximos pasos sugeridos

- implementar endpoints públicos del BFF (login, registro, perfil)
- conectar los clientes HTTP a los servicios de usuario y autenticación
- agregar validación en los DTOs
- agregar pruebas unitarias e integración para servicios y controladores
- documentar API con Swagger/OpenAPI


