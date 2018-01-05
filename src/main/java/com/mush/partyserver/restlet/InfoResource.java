/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mush.partyserver.restlet;

import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

/**
 *
 * @author Mirko Stancic, Dhimahi
 */
public class InfoResource extends ServerResource {

    @Get
    public StringRepresentation represent() {
        return new StringRepresentation("hello");
    }
}
