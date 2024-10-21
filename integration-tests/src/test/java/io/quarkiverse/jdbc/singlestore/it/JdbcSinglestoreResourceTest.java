package io.quarkiverse.jdbc.singlestore.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class JdbcSinglestoreResourceTest {

    @Test
    public void testHelloEndpoint() {
        given()
                .when().get("/jdbc-singlestore")
                .then()
                .statusCode(200)
                .body(is("Hello jdbc-singlestore"));
    }
}
