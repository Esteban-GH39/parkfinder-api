# parkfinder-api
Backend and REST API for the ParkFinder system—designed for managing parking facilities, reservations, users, payments, and notifications—built as a **modular monolith** (single Quarkus deployable, organized into bounded-context modules; internal communication between modules can be async via Kafka). See [BITACORA.md](../BITACORA.md#3-decisión-de-arquitectura-monolito-modular-vs-microservicios) for the reasoning.


# ParkFinder API (Backend)
Frontend: https://github.com/Esteban-GH39/parkfinder-app
Wiki / documentación: https://github.com/Esteban-GH39/parkfinder-api/wiki

## Stack

- **Lenguaje/Framework:** Java 21 + [Quarkus](https://quarkus.io) 3.39.2
- **API:** REST (Quarkus REST + Jackson)
- **Persistencia:** Hibernate ORM with Panache + JDBC PostgreSQL
- **Mensajería asíncrona:** SmallRye Reactive Messaging (Kafka)
- **Observabilidad:** SmallRye Health (`/q/health`)
- **Documentación de API:** SmallRye OpenAPI / Swagger UI (`/q/swagger-ui`)

## Requisitos

- JDK 21+ (no hace falta instalar Maven: el proyecto trae `mvnw`/`mvnw.cmd`)

## Ejecutar en modo desarrollo

```shell script
./mvnw quarkus:dev
```

Dev UI disponible en <http://localhost:8080/q/dev/> solo en modo dev.

## Compilar y empaquetar

```shell script
./mvnw package
```

Genera `target/quarkus-app/quarkus-run.jar`, ejecutable con:

```shell script
java -jar target/quarkus-app/quarkus-run.jar
```

## Estructura de paquetes

`src/main/java/com/parkfinder/` está organizado por módulo (bounded context), no por capa técnica:

- `users` — registro/autenticación de los dos tipos de usuario: **cliente** y **administrador**.
- `admin` — cuenta de negocio, alta y administración de parqueaderos (ubicación, zonas, tarifas, disponibilidad) por parte del administrador (dueño del parqueadero).
- `reservations` — búsqueda de disponibilidad y reserva en tiempo real por parte del cliente.
- `payments` — cobro de las reservas.
- `notifications` — confirmaciones/avisos, disparados de forma asíncrona (Kafka) a partir de eventos de los otros módulos.
- `common` — utilidades compartidas entre módulos (sin lógica de negocio de un módulo específico).
- `example` — clases de ejemplo generadas por Quarkus (`GreetingResource`, `MyEntity`, `MyLivenessCheck`, `MyMessagingApplication`), una por cada extensión instalada. **Se van eliminando** a medida que se implementa cada historia de usuario real; sirven de referencia de cómo usar REST, Panache, Health y Messaging en Quarkus.

## Estado

Proyecto en fase de scaffolding inicial (aún sin lógica de negocio).
