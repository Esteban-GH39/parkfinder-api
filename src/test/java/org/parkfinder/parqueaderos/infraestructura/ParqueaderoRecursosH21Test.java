package org.parkfinder.parqueaderos.infraestructura;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class ParqueaderoRecursosHU21Test {

    @Test
    public void testActualizarCuposExitoso() {
        String body = """
            {
              "idAdmin": 1,
              "cuposDisponibles": 15
            }
            """;

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .put("/kick/parqueaderos/1/cuposDisponibles")
                .then()
                .statusCode(200)
                .body("cuposDisponibles", is(15));
    }
}