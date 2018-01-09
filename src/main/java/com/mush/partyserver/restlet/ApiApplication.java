/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mush.partyserver.restlet;

import com.mush.partyserver.Config;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.resource.Directory;
import org.restlet.routing.Router;
import org.restlet.service.CorsService;

/**
 *
 * @author Cic
 */
public class ApiApplication extends Application {

    String htmlDir;

    public ApiApplication(Config config) {
        setName("RestApi");
        
        htmlDir = config.getRootUri();

        CorsService corsService = new CorsService();
        corsService.setAllowedOrigins(new HashSet<>(Arrays.asList("*")));
        corsService.setAllowedCredentials(true);
        corsService.setAllowingAllRequestedHeaders(true);
        corsService.setSkippingResourceForCorsOptions(true);

        getServices().add(corsService);
    }

    @Override
    public Restlet createInboundRoot() {
        Router router = new Router(getContext());

        router.attach("/info", InfoResource.class);
        
        String rootUri = Paths.get(htmlDir).toAbsolutePath().toUri().toString();
        
        Restlet directoryRouter = new Directory(getContext(), rootUri);
        
        router.attachDefault(directoryRouter);

        return router;
    }
    
}
