package at.htl.catalogue;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static io.restassured.RestAssured.given;

@QuarkusTest
class BookResourceMockTest {

    @InjectMock
    BookService service;

    @Test
    void notFoundIsMappedTo404() {
        Mockito.when(service.get(7)).thenThrow(new BookNotFoundException(7));

        given().when().get("/books/7").then().statusCode(404);
    }
}
