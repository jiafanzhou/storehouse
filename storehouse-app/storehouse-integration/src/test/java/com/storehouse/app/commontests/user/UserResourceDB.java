package com.storehouse.app.commontests.user;

import static com.storehouse.app.commontests.user.UserForTestsRepository.*;

import com.storehouse.app.user.services.UserServices;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Stateless
@Path("/DB/users")
@Produces(MediaType.APPLICATION_JSON)
public class UserResourceDB {
    @Inject
    private UserServices userServices;

    @POST
    public void addAll() {
        allUsers().forEach(userServices::add);
    }

    @POST
    @Path("/admin")
    public void addAdmin() {
        userServices.add(admin());
    }
}
