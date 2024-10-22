package io.quarkiverse.jdbc.singlestore.it.jpa;

import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.RestAssured;

/**
 * Test various JPA operations running in Quarkus
 */
@QuarkusTest
@TestProfile(JPATestProfile.class)
public class JPAFunctionalityTest {

    @Test
    public void testJPAFunctionalityFromServlet() throws Exception {
        RestAssured.when().get("/jpa/testfunctionality").then().body(is("OK"));
    }

}
