package com.storehouse.app.commontests.utils;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;

/**
 * This helper end point is only used here in the Integration test to clean the
 * database for each of the Arquillian IT tests.
 * 
 * @author ejiafzh
 *
 */
@Path("/DB")
public class DBResource {

    @Inject
    private TestRepositoryEJB testRepoEJB;

    @DELETE
    public void deleteAll() {
        testRepoEJB.deleteAll();
    }
}
