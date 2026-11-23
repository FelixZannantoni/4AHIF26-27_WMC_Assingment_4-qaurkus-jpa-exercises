package at.htl.catalogue;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

@QuarkusTest
@TestHTTPEndpoint(LoanResource.class)
class LoanResourceTest {

    static final UUID ADA = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Test
    void lendABook() {
        given()
          .contentType(ContentType.JSON)
          .body(new LoanRequest(2L, ADA))
          .when().post()
          .then()
             .statusCode(201)
             .body("title", equalTo("Solaris"))
             .body("member", equalTo("Ada"));

        given()
          .when().get()
          .then()
             .statusCode(200)
             .body("title", hasItem("Solaris"));
    }

    @Test
    void unknownMemberReturns404() {
        given()
          .contentType(ContentType.JSON)
          .body(new LoanRequest(1L, UUID.randomUUID()))
          .when().post()
          .then()
             .statusCode(404);
    }

    @Test
    void missingBookIdReturns400() {
        given()
          .contentType(ContentType.JSON)
          .body(Map.of("memberId", ADA.toString()))
          .when().post()
          .then()
             .statusCode(400);
    }
}
