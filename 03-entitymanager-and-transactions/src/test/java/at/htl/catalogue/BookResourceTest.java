package at.htl.catalogue;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;

@QuarkusTest
@TestHTTPEndpoint(BookResource.class)
class BookResourceTest {

    @Test
    void listReturnsBooks() {
        given()
          .when().get()
          .then()
             .statusCode(200)
             .contentType(ContentType.JSON)
             .body("size()", greaterThan(0))
             .body("title", hasItem("Dune"));
    }

    @Test
    void getByIdReturnsOneBook() {
        given()
          .when().get("/{id}", 1)
          .then()
             .statusCode(200)
             .body("author", equalTo("Herbert"));
    }

    @Test
    void createReturns201AndLocation() {
        given()
          .contentType(ContentType.JSON)
          .body(new Book(null, "Neuromancer", "Gibson", 1984))
          .when().post()
          .then()
             .statusCode(201)
             .header("Location", containsString("/books/"))
             .body("title", equalTo("Neuromancer"));
    }

    @Test
    void invalidBookReturns400() {
        given()
          .contentType(ContentType.JSON)
          .body(Map.of("title", "", "author", "Herbert", "year", 12))
          .when().post()
          .then()
             .statusCode(400);
    }

    @Test
    void unknownIdReturns404() {
        given()
          .when().get("/{id}", 9999)
          .then()
             .statusCode(404)
             .body("message", containsString("9999"));
    }

    @Test
    void createThenGet() {
        Book created = given()
                .contentType(ContentType.JSON)
                .body(new Book(null, "Foundation", "Asimov", 1951))
                .when().post()
                .then().statusCode(201)
                .extract().as(Book.class);

        given()
          .when().get("/{id}", created.id())
          .then().statusCode(200).body("title", equalTo("Foundation"));
    }

    @Test
    void genreAndIsbnAreStored() {
        Book created = given()
                .contentType(ContentType.JSON)
                .body(new Book(null, "The Hobbit", "Tolkien", 1937, Genre.FANTASY, "9780547928227"))
                .when().post()
                .then().statusCode(201)
                .extract().as(Book.class);

        given()
          .when().get("/{id}", created.id())
          .then()
             .statusCode(200)
             .body("genre", equalTo("FANTASY"))
             .body("isbn", equalTo("9780547928227"));
    }

    @Test
    void invalidIsbnReturns400() {
        given()
          .contentType(ContentType.JSON)
          .body(new Book(null, "Dune", "Herbert", 1965, Genre.SCIENCE_FICTION, "123"))
          .when().post()
          .then()
             .statusCode(400);
    }
}
