package at.htl.catalogue;

import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.List;

@Path("/books")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BookResource {

    private final BookService service;

    public BookResource(BookService service) {
        this.service = service;
    }

    @GET
    public List<Book> list(@QueryParam("author") String author,
                           @QueryParam("genre") Genre genre,
                           @QueryParam("page") @DefaultValue("0") int page,
                           @QueryParam("size") @DefaultValue("20") int size) {
        if (genre != null) {
            return service.findByGenre(genre);
        }
        return service.find(author, page, size);
    }

    @GET
    @Path("/{id}")
    public Book get(@PathParam("id") long id) {
        return service.get(id);
    }

    @POST
    public Response create(@Valid Book book) {
        Book saved = service.add(book);
        return Response.created(URI.create("/books/" + saved.id())).entity(saved).build();
    }

    @DELETE
    @RolesAllowed("admin")
    @Path("/{id}")
    public void delete(@PathParam("id") long id) {
        service.remove(id);
    }
}
