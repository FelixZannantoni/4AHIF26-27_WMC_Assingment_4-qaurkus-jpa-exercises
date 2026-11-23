package at.htl.catalogue;

import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

public class ErrorHandlers {

    @ServerExceptionMapper
    public RestResponse<ErrorDto> notFound(BookNotFoundException e) {
        return RestResponse.status(Response.Status.NOT_FOUND, new ErrorDto(e.getMessage()));
    }

    @ServerExceptionMapper
    public RestResponse<ErrorDto> alreadyLent(BookAlreadyLentException e) {
        return RestResponse.status(Response.Status.CONFLICT, new ErrorDto(e.getMessage()));
    }
}
