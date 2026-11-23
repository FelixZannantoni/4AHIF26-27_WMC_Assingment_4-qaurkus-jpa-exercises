package at.htl.catalogue;

import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/loans")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LoanResource {

    private final LoanService service;

    public LoanResource(LoanService service) {
        this.service = service;
    }

    @GET
    public List<Loan> list() {
        return service.all();
    }

    @POST
    public Response lend(@Valid LoanRequest request) {
        Loan loan = service.lend(request);
        return Response.status(Response.Status.CREATED).entity(loan).build();
    }
}
