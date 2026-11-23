package at.htl.catalogue;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;

@QuarkusTest
class AuthorResourceTest {

    @Test
    void listContainsSeededAuthors() {
        given()
          .when().get("/authors")
          .then()
             .statusCode(200)
             .body("name", hasItems("Herbert", "Lem"));
    }

    @Test
    void booksOfAnAuthor() {
        given()
          .when().get("/authors/{id}/books", 1)
          .then()
             .statusCode(200)
             .body("title", hasItem("Dune"))
             .body("[0].author", equalTo("Herbert"));
    }

    @Test
    void unknownAuthorReturns404() {
        given().when().get("/authors/{id}/books", 9999).then().statusCode(404);
    }

    @Test
    void newBookCreatesItsAuthor() {
        given()
          .contentType(ContentType.JSON)
          .body(new Book(null, "The Left Hand of Darkness", "Le Guin", 1969, Genre.SCIENCE_FICTION, null))
          .when().post("/books")
          .then().statusCode(201);

        given()
          .when().get("/authors")
          .then()
             .statusCode(200)
             .body("name", hasItem("Le Guin"));
    }

    @Test
    void statsCountBooksPerAuthor() {
        given()
          .when().get("/authors/stats")
          .then()
             .statusCode(200)
             .body("find { it.name == 'Lem' }.bookCount", equalTo(1));
    }

    @Test
    void authorsComeWithTheirTitles() {
        given()
          .when().get("/authors")
          .then()
             .statusCode(200)
             .body("find { it.name == 'Herbert' }.titles", hasItem("Dune"));
    }
}
