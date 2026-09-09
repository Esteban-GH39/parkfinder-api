package com.parkfinder.users;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

/**
 * Prueba de HU-01 (registro de cliente). Requiere una base de datos real
 * (Quarkus Dev Services levanta Postgres vía Docker automáticamente) — en
 * una máquina sin Docker, estos tests no corren.
 */
@QuarkusTest
class UsuariosResourceTest {

    @Test
    void registrarClienteConDatosValidos_devuelve201() {
        given()
                .contentType("application/json")
                .body("""
                        {
                          "nombre": "Ana Pérez",
                          "email": "ana@example.com",
                          "password": "clave1234"
                        }
                        """)
                .when().post("/usuarios/clientes")
                .then()
                .statusCode(201)
                .body("email", is("ana@example.com"))
                .body("rol", is("CLIENTE"));
    }

    @Test
    void registrarClienteConEmailDuplicado_devuelve409() {
        String body = """
                {
                  "nombre": "Carlos Ruiz",
                  "email": "carlos@example.com",
                  "password": "clave1234"
                }
                """;

        given().contentType("application/json").body(body)
                .when().post("/usuarios/clientes")
                .then().statusCode(201);

        given().contentType("application/json").body(body)
                .when().post("/usuarios/clientes")
                .then().statusCode(409);
    }

    @Test
    void registrarClienteConPasswordDebil_devuelve400() {
        given()
                .contentType("application/json")
                .body("""
                        {
                          "nombre": "Luis Gómez",
                          "email": "luis@example.com",
                          "password": "abc"
                        }
                        """)
                .when().post("/usuarios/clientes")
                .then()
                .statusCode(400);
    }
}
