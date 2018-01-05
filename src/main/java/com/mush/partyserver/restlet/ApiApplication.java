/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mush.partyserver.restlet;

import java.util.Arrays;
import java.util.HashSet;
import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Router;
import org.restlet.service.CorsService;

/**
 *
 * @author Cic
 */
public class ApiApplication extends Application {


    public ApiApplication() {
        setName("RestApi");

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

        router.attach("/", InfoResource.class);

        return router;
    }
}
