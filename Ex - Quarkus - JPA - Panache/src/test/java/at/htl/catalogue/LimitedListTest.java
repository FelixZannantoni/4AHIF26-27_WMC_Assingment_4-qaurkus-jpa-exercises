package at.htl.catalogue;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
@TestProfile(FewResultsProfile.class)
class LimitedListTest {

    @Test
    void listIsLimitedToTwoBooks() {
        given()
          .contentType(ContentType.JSON)
          .body(Map.of("title", "Ubik", "author", "Dick", "year", 1969))
          .when().post("/books")
          .then().statusCode(201);

        given()
          .when().get("/books")
          .then()
             .statusCode(200)
             .body("size()", equalTo(2));
    }
}
