package com.revolut.revolutaccountmanager.resource;

import com.revolut.revolutaccountmanager.model.CreateUserRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("user")
public class UserResource {

    @Inject

    private static Logger LOG = LoggerFactory.getLogger(UserResource.class);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("create")
    public Response createUser(CreateUserRequest createUserRequest) {
        LOG.error("hey {}", createUserRequest);
        return Response.ok().build();

    }
}