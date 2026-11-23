package at.htl.catalogue;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;

@QuarkusTest
class LoanTransactionTest {

    static final UUID ADA = UUID.fromString("00000000-0000-0000-0000-000000000001");
    static final UUID ALAN = UUID.fromString("00000000-0000-0000-0000-000000000002");

    private long createBook() {
        return given()
                .contentType(ContentType.JSON)
                .body(new Book(null, "Neuromancer", "Gibson", 1984))
                .when().post("/books")
                .then().statusCode(201)
                .extract().as(Book.class).id();
    }

    @Test
    void lendingALentBookIsRolledBackButAudited() {
        long bookId = createBook();

        given()
          .contentType(ContentType.JSON)
          .body(new LoanRequest(bookId, ADA))
          .when().post("/loans")
          .then().statusCode(201);

        given()
          .contentType(ContentType.JSON)
          .body(new LoanRequest(bookId, ALAN))
          .when().post("/loans")
          .then()
             .statusCode(409)
             .body("message", containsString("already lent"));

        // rollback: Alan's loan was inserted, then undone
        given()
          .when().get("/loans")
          .then()
             .statusCode(200)
             .body("findAll { it.bookId == " + bookId + " }", hasSize(1));

        // REQUIRES_NEW: the audit entry of the failed attempt survived
        given()
          .when().get("/audit")
          .then()
             .statusCode(200)
             .body("text", hasItem("lend book " + bookId + " to member " + ALAN));
    }
}
