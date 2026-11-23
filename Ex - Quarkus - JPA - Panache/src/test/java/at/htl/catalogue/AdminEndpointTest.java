package at.htl.catalogue;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
class AdminEndpointTest {

    private long createBook() {
        return given()
                .contentType(ContentType.JSON)
                .body(new Book(null, "Temp", "Nobody", 2000))
                .when().post("/books")
                .then().statusCode(201)
                .extract().as(Book.class).id();
    }

    @Test
    @TestSecurity(user = "alice", roles = "admin")
    void adminCanDelete() {
        long id = createBook();

        given().when().delete("/books/{id}", id).then().statusCode(204);
    }

    @Test
    @TestSecurity(user = "bob", roles = "pupil")
    void otherRoleIsForbidden() {
        long id = createBook();

        given().when().delete("/books/{id}", id).then().statusCode(403);
    }

    @Test
    void anonymousCannotDelete() {
        given().when().delete("/books/1").then().statusCode(401);
    }
}
