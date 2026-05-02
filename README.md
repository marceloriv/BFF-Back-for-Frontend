# BFF Back for Frontend

Backend tipo BFF para servir de capa intermedia entre un frontend y servicios de autenticación/usuario.

## Descripción

Este proyecto está construido con Spring Boot y Java 17. La estructura actual está pensada para centralizar la lógica de acceso, seguridad y consumo de servicios externos desde una sola API.

En esta base ya están creados los paquetes para:

- autenticación
- configuración de clientes HTTP
- seguridad
- DTOs de auth y usuario
- manejo global de errores

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

El proyecto está en una fase inicial. La base estructural está lista, pero todavía faltan implementar los endpoints, servicios y clientes hacia los sistemas externos.

## Próximos pasos sugeridos

- definir los endpoints públicos del BFF
- conectar los clientes HTTP a los servicios reales
- completar la seguridad JWT
- agregar pruebas para la capa de servicio y controladores
