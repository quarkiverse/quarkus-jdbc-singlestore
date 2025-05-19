package io.quarkiverse.jdbc.singlestore.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class JdbcSinglestoreResourceTest {

    @Test
    public void testAgroalEndpoint() {
        given()
                .when().get("/jdbc-singlestore/agroal")
                .then()
                .statusCode(200)
                .body(is("1/leo/"));
    }

    @Test
    public void testConnectionEndpoint() {
        given()
                .when().get("/jdbc-singlestore/connection")
                .then()
                .statusCode(200)
                .body(is("1/leo/"));

    }
}
