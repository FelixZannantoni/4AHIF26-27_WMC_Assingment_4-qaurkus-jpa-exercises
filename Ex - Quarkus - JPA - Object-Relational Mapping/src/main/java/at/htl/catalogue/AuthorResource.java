package at.htl.catalogue;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/authors")
@Produces(MediaType.APPLICATION_JSON)
public class AuthorResource {

    private final AuthorService service;

    public AuthorResource(AuthorService service) {
        this.service = service;
    }

    @GET
    public List<Author> list() {
        return service.all();
    }

    @GET
    @Path("/{id}/books")
    public List<Book> books(@PathParam("id") long id) {
        return service.books(id);
    }
}
